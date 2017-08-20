package index.alchemy.core.run;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.jooq.lambda.Unchecked;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import index.alchemy.core.asm.transformer.MeowTweaker;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.gradle.GradleStartCommon;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.*;

public class GradleStartAlchemy extends GradleStartCommon {
	
	static { if (GradleStartAlchemy.class.getClassLoader() instanceof LaunchClassLoader) throw new RuntimeException(); }
	
	static { LOGGER = LogManager.getLogger(GradleStartAlchemy.class.getSimpleName()); }
	
	public static final double JAVA_VERSION = Optional.of(System.getProperty("java.specification.version")).map(Double::parseDouble).get();
	
	private static final sun.misc.Unsafe unsafe = Unchecked.supplier(GradleStartAlchemy::getUnsafe).get();
	
	private static final sun.misc.Unsafe unsafe() { return unsafe; }
	
	private static sun.misc.Unsafe getUnsafe() throws PrivilegedActionException {
		return AccessController.doPrivileged(new PrivilegedExceptionAction<sun.misc.Unsafe>() {
			
			@Override
			public sun.misc.Unsafe run() throws Exception {
				Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				return (sun.misc.Unsafe) theUnsafe.get(null);
			}
			
	   });
	}
	
	static {
		if (JAVA_VERSION >= '⑨' / 1000 || true)
			try {
				MeowTweaker.setUnsafe(unsafe());
				String clazz = "index.alchemy.util.ReflectionHelper";
				byte basicClass[] = IOUtils.toByteArray(MeowTweaker.class.getClassLoader().getResourceAsStream(
						clazz.replace('.', '/') + ".class"));
				basicClass = MeowTweaker.Sayaka().transform(clazz, clazz, basicClass);
				unsafe().defineClass(clazz, basicClass, 0, basicClass.length, GradleStartAlchemy.class.getClassLoader(),
						GradleStartAlchemy.class.getProtectionDomain());
			} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void main(String[] args) throws Throwable {
		hackNatives();
		new GradleStartAlchemy().launch(args);
	}

	@Override
	protected String getBounceClass() {
		return "net.minecraft.launchwrapper.Launch";
	}

	@Override
	protected String getTweakClass() {
		return Side.valueOf(Tool.isEmptyOr(System.getProperty("index.alchemy.run.side"), "client").toUpperCase(Locale.ENGLISH)).isClient() ?
				"net.minecraftforge.fml.common.launcher.FMLTweaker" : "net.minecraftforge.fml.common.launcher.FMLServerTweaker";
	}

	@Override
	protected void setDefaultArguments(Map<String, String> argMap) {
		argMap.put("version", MC_VERSION);
		argMap.put("assetIndex", Tool.get(MC_VERSION, "(.*?\\.[0-9]*)"));
		argMap.put("assetsDir", Tool.isEmptyOr(System.getProperty("index.alchemy.gradle.path"), "") + ".gradle/caches/minecraft/assets");
		argMap.put("accessToken", "FML");
		argMap.put("userProperties", "{}");
		argMap.put("username", null);
		argMap.put("password", null);
	}
	
	@Override
	protected void preLaunch(Map<String, String> argMap, List<String> extras) { }
	
	protected void launch(String[] args) throws Throwable {
		Class<GradleStartCommon> common = GradleStartCommon.class;
		System.setProperty("net.minecraftforge.gradle.GradleStart.srgDir", Tool.<File>$(common, "SRG_DIR").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg", Tool.<File>$(common, "SRG_NOTCH_SRG").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-mcp", Tool.<File>$(common, "SRG_NOTCH_MCP").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp", Tool.<File>$(common, "SRG_SRG_MCP").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-srg", Tool.<File>$(common, "SRG_MCP_SRG").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-notch", Tool.<File>$(common, "SRG_MCP_NOTCH").getCanonicalPath());
		System.setProperty("net.minecraftforge.gradle.GradleStart.csvDir", Tool.<File>$(common, "CSV_DIR").getCanonicalPath());
		setDefaultArguments($(this, "argMap"));
		$(this, "parseArgs", args);
		preLaunch($(this, "argMap"), $(this, "extras"));
		System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
		searchCoremods(this);
		args = $(this, "getArgs");
		$(this, "argMap<", null);
		$(this, "extras<", null);
		System.gc();
		$("L" + getBounceClass(), "new");
		Launch.blackboard.put("Tweaks", Lists.newArrayList());
		$("L" + getBounceClass(), "main", args);
	}

	private static void hackNatives() {
		String paths = System.getProperty("java.library.path");
		String nativesDir = Tool.isEmptyOr(System.getProperty("index.alchemy.gradle.path"), "") +
				".gradle/caches/minecraft/net/minecraft/natives/" + MC_VERSION;
		paths = Strings.isNullOrEmpty(paths) ? nativesDir : paths + File.pathSeparator + nativesDir;
		System.setProperty("java.library.path", paths);
		$(ClassLoader.class, "sys_paths<", null);
	}
	
	public static void searchCoremods(GradleStartCommon common) throws Exception {
		List<String> extras = $(common, "extras");
		for (URL url : ((URLClassLoader) GradleStartCommon.class.getClassLoader()).getURLs()) {
			if (!url.getProtocol().startsWith("file"))
				continue;
			File coreMod = new File(url.toURI().getPath());
			if (coreMod.exists() && coreMod.getName().endsWith("jar"))
				try (JarFile jar = new JarFile(coreMod)) {
					if (jar.getManifest() != null)
						$("Lnet.minecraftforge.fml.common.asm.transformers.ModAccessTransformer", "addJar", jar);
				}
		}
		HashSet coremods = Sets.newHashSet();
		if (!Strings.isNullOrEmpty(System.getProperty("fml.coreMods.load")))
			coremods.addAll(Splitter.on(',').splitToList(System.getProperty("fml.coreMods.load")));
		System.setProperty("fml.coreMods.load", Joiner.on(',').join(coremods));
		if (!Strings.isNullOrEmpty($(common, "getTweakClass"))) {
			extras.add("--tweakClass");
			extras.add("net.minecraftforge.gradle.tweakers.CoremodTweaker");
		}
	}

}
