package index.alchemy.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.jws.WebMethod;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.gson.Gson;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Config.Handle.Type;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result.Build;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result.Build.Artifact;
import index.alchemy.util.Http;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import net.minecraftforge.common.util.EnumHelper;

import static index.alchemy.core.AlchemyConstants.*;

@Alpha
public class AlchemyUpdateManager {
	
	private static final String CATEGORY_UPDATE = "update";
	
	public static final Logger logger = LogManager.getLogger(AlchemyUpdateManager.class.getSimpleName());
	
	public static class VersionInfo {
		
		public final String name, url, relativePath, versionType, jarType, mcVersion, suffix;
		public final int[] version;
		
		public final boolean isDLC;
		public final String dlcName;
		
		private VersionInfo(String relativePath, String url) throws Exception {
			this.url = url;
			this.relativePath = relativePath;
			name = Tool.get(relativePath, ".*/(.*?)\\.");
			suffix = Tool.get(relativePath, ".*\\.(.*)");
			String nodes[] = name.split("-");
			int next = 0;
			if (!nodes[next++].equals(MOD_NAME))
				throw new RuntimeException("name != " + MOD_NAME);
			versionType = nodes[next++];
			isDLC = nodes[next++].equals("dlc");
			dlcName = isDLC ? nodes[next++] : "";
			mcVersion = nodes[next++];
			version = Tool.stringToIntArray(nodes[next++]);
			jarType = nodes[next++];
		}
		
		@Nullable
		public static VersionInfo formBuild(Build build, @Nullable String dlcName, String versionType, String jarType) {
			VersionInfo info = null;
			for (Artifact artifact : build.artifacts)
				try {
					info = new VersionInfo(artifact.relativePath, build.url);
					if (info.dlcName.equals(dlcName) && info.versionType.equals(versionType) && info.jarType.equals(jarType))
						return info;
				} catch (Exception e) { }
			return null;
		}
		
		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
		
	}
	
	public static enum JenkinsCI {
		
		MICKEY("http", "ci.mickey.moe"),
		INFINITY_STUDIO("https", "ci.infstudio.net");
		
		public static final class Result {
			
			public static final class Build {

				public static final class Artifact {
					
					public String relativePath;
					
				}
				
				public static final class ChangeSet {
					
					public static final class Item {
						
						public String msg;
						
					}
					
					public Item items[];
					
				}
				
				public Artifact artifacts[];
				public long timestamp;
				public String url, result;
				public ChangeSet changeSet;
				
			}
			
			public Build builds[];
			
		}
		
		public static final String HANDLE_JENKINS_CI = "JenkinsCI", SUCCESS = "SUCCESS", UNIVERSAL = "universal", SEPARATOT = "://";
		
		public static final Class TYPES[] = { String.class, String.class };
		
		public static final String tree_api = makeTreeApi(Result.class);
		
		@Config(handle = HANDLE_JENKINS_CI, category = CATEGORY_UPDATE, comment = "Custom Jenkins CI URL.")
		private static JenkinsCI custom = MICKEY;
		
		public static JenkinsCI getCustom() {
			return custom;
		}
		
		@Config.Handle(name = HANDLE_JENKINS_CI, type = Type.MAKE)
		public static JenkinsCI makeJenkinsCI(String url) {
			if (url.isEmpty())
				return MICKEY;
			String scheme, host;
			int index = url.indexOf(SEPARATOT);
			if (index == -1) {
				scheme = "http";
				host = url;
			} else {
				scheme = url.substring(0, index);
				host = url.substring(index + SEPARATOT.length());
			}
			return EnumHelper.addEnum(JenkinsCI.class, "custom", TYPES, scheme, host);
		}
		
		@Config.Handle(name = HANDLE_JENKINS_CI, type = Type.SAVE)
		public static String saveJenkinsCI(JenkinsCI ci) {
			if (ci == null)
				ci = MICKEY;
			return ci.scheme + SEPARATOT + ci.host;
		}
		
		public final String scheme, host;
		
		private JenkinsCI(String scheme, String host) {
			this.scheme = scheme;
			this.host = host;
		}
		
		@Override
		public String toString() {
			return saveJenkinsCI(this);
		}
		
		private static String makeTreeApi(Class<?> clazz) {
			return Joiner.on(',').join(Arrays.stream(clazz.getFields()).map(JenkinsCI::makeTreeApi).toArray());
		}
		
		private static String makeTreeApi(Field field) {
			return Tool.isBasics(field.getType()) ? field.getName() :
				merge(field.getName(), Arrays.stream((field.getType().isArray() ? field.getType().getComponentType() :
					field.getType()).getFields()).map(JenkinsCI::makeTreeApi).toArray());
		}
		
		public static String merge(String root, Object args[]) {
			return root + '[' + Joiner.on(',').skipNulls().join(args) + ']';
		}
		
		public String getVersionInfoURL(String job) {
			return scheme + "://" + host + "/job/" + job + "/api/json?tree=" + tree_api;
		}
		
		@Nullable
		@WebMethod
		public String getVersionInfoJson(String job) throws Exception {
//			String uri = getVersionInfoURL(job);
			Http.Result result = Http.get(getVersionInfoURL(job), Http.shadowsocks);
			return result.getCode() == Http.Code.OK ? result.getText() : null;
		}
		
		@Nullable
		public Result getVersionInfoResult(String job) throws Exception {
			String json = getVersionInfoJson(job);
			return json != null ? new Gson().fromJson(json, Result.class) : null;
		}
		
	}
	
	@Config(category = "update", comment = "Automatic update Alchemy mod when the game starts.")
	private static boolean auto_update = false;
	
	@Nullable
	public static VersionInfo getLatestVersionInfo(String job, @Nullable String dlcName, String jarType) {
		for (JenkinsCI ci : JenkinsCI.values())
			try {
				Result result = ci.getVersionInfoResult(job);
				for (Build build : result.builds)
					if (build.result.equals(JenkinsCI.SUCCESS))
						return VersionInfo.formBuild(build, dlcName, MC_VERSION, jarType);
			} catch (Exception e) {
				logger.warn("Failed to get version information from: " + ci.host, e);
				continue;
			}
		return null;
	}
	
	@SuppressWarnings("unused")
	@Alpha
	public static void invoke(String job, String now_version, @Nullable String dlcName, File file) {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		
		if (true || AlchemyModLoader.is_modding || !auto_update)
			return;
		
		VersionInfo info = getLatestVersionInfo(job, dlcName, JenkinsCI.UNIVERSAL);
		if (info == null) {
			logger.warn("AlchemyUpdateManager.invoke() -> info is null");
			return;
		}
		
		int now[] = Tool.stringToIntArray(now_version);
		if (now.length > info.version.length)
			return;
		if (now[now.length - 1] < info.version[now.length - 1])
			update(info, file);
	}
	
	@Alpha
	@Nullable
	@WebMethod
	public static File update(VersionInfo info, File file) {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		
		logger.info("Starting update ...");
		String url = info.url + "artifact/" + info.relativePath;
		File result = Http.downloadFromUrl(url, info.name + "." + info.suffix, file.getPath(), 3000, Http.shadowsocks);
		if (result != null) {
			file.deleteOnExit();
			return result;
		}
		return file;
	}

}