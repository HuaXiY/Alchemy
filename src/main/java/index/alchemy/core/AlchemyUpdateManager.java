package index.alchemy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import com.google.common.base.Joiner;
import com.google.gson.Gson;

import index.alchemy.api.Alway;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Config.Handle.Type;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result.Build;
import index.alchemy.core.AlchemyUpdateManager.JenkinsCI.Result.Build.Artifact;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import static index.alchemy.core.AlchemyConstants.*;

public class AlchemyUpdateManager {
	
	private static class FileDownloadResponseHandler implements ResponseHandler<File> {

		private final File target;

		public FileDownloadResponseHandler(File target) {
			this.target = target;
		}

		@Override
		public File handleResponse(HttpResponse response) throws IOException {
			boolean client = Alway.runOnClient();
			int size = Integer.valueOf(response.getFirstHeader("content-length").getValue());
			ProgressBar bar = null;
			if (client) {
				bar = ProgressManager.push("AlchemyUpdateManager", (int) size);
			}
			
			InputStream input = response.getEntity().getContent();
			FileOutputStream output = new FileOutputStream(target);
			byte buffer[] = new byte[4096];
			long count = 0;
			int len = 0;
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer, 0, len);
				count += len;
				if (client) {
					float progress = count / (float) size * 100;
					String display =  String.format("%.1f", progress) + "%";
					display = "Updating: " + Tool.getString(' ', 5 - display.length()) + display;
					for (int i = 0; i < len; i++)
						bar.step(display);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
			
			if (client) {
				ProgressManager.pop(bar);
			}
			IOUtils.closeQuietly(output);
			return target;
		}
		
	}
	
	private static final String CATEGORY_UPDATE = "update";
	
	public static final Logger logger = LogManager.getLogger(AlchemyUpdateManager.class.getSimpleName());
	
	public static enum JenkinsCI {
		
		MICKEY("http", "ci.mickey.moe"),
		INFINITY_STUDIO("https", "ci.infstudio.net");
		
		public static final class Result {
			
			public static final class Build {

				public static final class Artifact {
					
					public String relativePath;
					
				}
				
				public static final class ChangeSet {
					
					public static final class item {
						
						public String msg;
						
					}
					
					public item items[];
					
				}
				
				public Artifact artifacts[];
				public long timestamp;
				public String url, result;
				public ChangeSet changeSet;
				
			}
			
			public Build builds[];
			
		}
		
		public static final String NAME = "JenkinsCI", SUCCESS = "SUCCESS", UNIVERSAL = "universal", SEPARATOT = "://";
		
		public static final Class TYPES[] = {String.class, String.class};
		
		public static final String tree_api = makeTreeApi(Result.class);
		
		@Config(handle = NAME, comment = CATEGORY_UPDATE, category = "Custom Jenkins CI URL.")
		private static JenkinsCI custom;
		
		public static JenkinsCI getCustom() {
			return custom;
		}
		
		@Nullable
		@Config.Handle(name = NAME, type = Type.MAKE)
		public static JenkinsCI makeJenkinsCI(String url) {
			if (url.isEmpty())
				return null;
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
		
		@Config.Handle(name = NAME, type = Type.SAVE)
		public static String saveJenkinsCI(JenkinsCI ci) {
			return ci == null ? "" : ci.scheme + SEPARATOT + ci.host;
		}
		
		public final String scheme, host;
		
		private JenkinsCI(String scheme, String host) {
			this.scheme = scheme;
			this.host = host;
		}
		
		private static String makeTreeApi(Class<?> clazz) {
			List<String> list = new LinkedList<String>();
			for (Field field : clazz.getFields())
				list.add(makeTreeApi(field));
			return Joiner.on(',').join(list);
		}
		
		private static String makeTreeApi(Field field) {
			List<String> list = new LinkedList<String>();
			if (Tool.isBasics(field.getType()))
				return field.getName();
			else {
				List<String> args = new LinkedList<String>();
				Class<?> clazz = field.getType();
				for (Field f : clazz.isArray() ? clazz.getComponentType().getFields() : clazz.getFields())
					args.add(makeTreeApi(f));
				return merge(field.getName(), args);
			}
		}
		
		public static String merge(String root, List<String> args) {
			StringBuilder builder = new StringBuilder(root).append('[');
			Joiner.on(',').skipNulls().appendTo(builder, args);
			return builder.append(']').toString();
		}
		
		@Nullable
		public URI getVersionInfoURI(String job) {
			try {
				return new URIBuilder()
						.setScheme(scheme)
						.setHost(host)
						.setPath("/job/" + MOD_ID + "/api/json")
						.setParameter("tree", tree_api)
						.build();
			} catch (URISyntaxException e) {
				AlchemyRuntimeException.onException(e);
			}
			return null;
		}
		
		@Nullable
		public String getVersionInfoJson(String job) throws Exception {
			URI uri = getVersionInfoURI(job);
			if (uri != null) {
				HttpGet httpGet = new HttpGet(uri);
				CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse response = httpclient.execute(httpGet);
				try {
					if (response.getStatusLine().getStatusCode() == 200)
						return EntityUtils.toString(response.getEntity());
				} finally {
					IOUtils.closeQuietly(httpclient);
				}
			}
			return null;
		}
		
		@Nullable
		public Result getVersionInfoResult(String job) throws Exception {
			String json = getVersionInfoJson(job);
			if (json != null) {
				return new Gson().fromJson(json, Result.class);
			}
			return null;
		}
		
	}
	
	@Config(category = "update", comment = "Automatic update Alchemy mod when the game starts")
	private static boolean auto_update = false;
	
	@Nullable
	public static Build getLatestVersionInfo(String job) {
		for (JenkinsCI ci : JenkinsCI.values())
			try {
				Result result = ci.getVersionInfoResult(job);
				for (Build build : result.builds)
					if (build.result.equals(JenkinsCI.SUCCESS))
						return build;
			} catch (Exception e) {
				logger.warn("Failed to get version information from " + ci.host, e);
				continue;
			}
		return null;
	}
	
	public static void invoke(String job) {
		if (AlchemyModLoader.is_modding || !auto_update)
			return;
		Build build = getLatestVersionInfo(job);
		if (build == null) {
			logger.warn("AlchemyUpdateManager.invoke() -> build is null");
			return;
		}
		String version = null;
		int index = -1;
		for (Artifact artifact : build.artifacts) {
			index++;
			if (artifact.relativePath.contains(JenkinsCI.UNIVERSAL)) {
				version = Tool.get(artifact.relativePath, MC_VERSION + "-(.*\\..*?)-" + JenkinsCI.UNIVERSAL);
				break;
			}
		}
		if (version == null) {
			logger.warn("AlchemyUpdateManager.invoke() -> version is null");
			return;
		}
		if (DEV_VERSION.equals("?") || version.equals("?")) {
			logger.warn("AlchemyUpdateManager.invoke() -> DEV_VERSION || version unknown");
			return;
		}
		try {
			if (Integer.valueOf(DEV_VERSION) < Integer.valueOf(version))
				update(build, index);
		} catch (NumberFormatException e) {
			logger.warn("AlchemyUpdateManager.invoke() -> DEV_VERSION: " + DEV_VERSION + ", version: " + version, e);
		}
	}
	
	private static void update(Build build, int index) {
		logger.info("Start update ...");
		URI uri;
		try {
			uri = new URI(build.url + "artifact/" + build.artifacts[index].relativePath);
		} catch (URISyntaxException e) {
			logger.warn("AlchemyUpdateManager.update() -> URISyntaxException", e);
			return;
		}
		HttpGet httpGet = new HttpGet(uri);
		CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
		try {
			File downloaded = httpclient.execute(httpGet, new FileDownloadResponseHandler(
					new File(AlchemyModLoader.mc_dir + "/mods", Tool.get(build.artifacts[index].relativePath, ".*/(.*?\\.jar)"))));
			AlchemyModLoader.deleteOnExit();
			AlchemyModLoader.restart();
		} catch (IOException e) {
			logger.warn("AlchemyUpdateManager.update() -> IOException", e);
		}
		IOUtils.closeQuietly(httpclient);
	}
	
	public static void main(String[] args) throws Exception {
		auto_update = true;
		invoke(MOD_ID);
	}

}