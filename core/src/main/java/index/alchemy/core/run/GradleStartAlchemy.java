package index.alchemy.core.run;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.RegEx;

import index.alchemy.util.$;
import index.alchemy.util.FunctionHelper;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.gradle.GradleForgeHacks;
import net.minecraftforge.gradle.GradleStartCommon;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.Unsafe;

import static index.alchemy.core.AlchemyConstants.MC_VERSION;
import static index.alchemy.util.$.$;

public class GradleStartAlchemy extends GradleStartCommon {
    
    private static class UnsafeShared extends $ {
        
        protected static void markUnsafe(sun.misc.Unsafe unsafe) {
            $.markUnsafe(unsafe);
        }
        
    }
    
    static { if (GradleStartAlchemy.class.getClassLoader() instanceof LaunchClassLoader) throw new RuntimeException(); }
    
    static { LOGGER = LogManager.getLogger(GradleStartAlchemy.class.getSimpleName()); }
    
    protected static Logger logger() { return LOGGER; }
    
    public static final double JAVA_VERSION = Optional.of(System.getProperty("java.specification.version")).map(Double::parseDouble).get();
    
    private static final sun.misc.Unsafe unsafe = FunctionHelper.onThrowableSupplier(GradleStartAlchemy::getUnsafe, FunctionHelper::rethrowVoid).get();
    
    private static sun.misc.Unsafe unsafe() { return unsafe; }
    
    static { UnsafeShared.markUnsafe(unsafe()); }
    
    private static sun.misc.Unsafe getUnsafe() throws PrivilegedActionException {
        return AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        });
    }
    
    private static String isEmptyOr(String str, String or) {
        return str == null || str.isEmpty() ? or : str;
    }
    
    private static String get(String str, @RegEx String key) {
        Matcher matcher = Pattern.compile(key).matcher(str);
        return matcher.find() ? matcher.group(1) : "";
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
        return Side.valueOf(isEmptyOr(System.getProperty("index.alchemy.run.side"), "client").toUpperCase(Locale.ENGLISH)).isClient() ?
                "net.minecraftforge.fml.common.launcher.FMLTweaker" : "net.minecraftforge.fml.common.launcher.FMLServerTweaker";
    }
    
    @Override
    protected void setDefaultArguments(Map<String, String> argMap) {
        argMap.put("version", MC_VERSION);
        argMap.put("assetIndex", get(MC_VERSION, "(.*?\\.[^.]*)"));
        argMap.put("assetsDir", isEmptyOr(System.getProperty("index.alchemy.gradle.path"), "") + "/.gradle/caches/minecraft/assets");
        argMap.put("accessToken", "FML");
        argMap.put("userProperties", "{}");
        argMap.put("username", null);
        argMap.put("password", null);
    }
    
    @Override
    protected void preLaunch(Map<String, String> argMap, List<String> extras) {
        if (!Strings.isNullOrEmpty(argMap.get("password"))) {
            GradleStartCommon.LOGGER.info("Password found, attempting login");
            attemptLogin(argMap);
        }
    }
    
    private void attemptLogin(Map<String, String> argMap) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(argMap.get("username"));
        auth.setPassword(argMap.get("password"));
        argMap.put("password", null);
        
        try {
            auth.logIn();
        } catch (AuthenticationException e) {
            LOGGER.error("-- Login failed!  " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        LOGGER.info("Login Succesful!");
        argMap.put("accessToken", auth.getAuthenticatedToken());
        argMap.put("uuid", auth.getSelectedProfile().getId().toString().replace("-", ""));
        argMap.put("username", auth.getSelectedProfile().getName());
        argMap.put("userType", auth.getUserType().getName());
        
        // 1.8 only apperantly.. -_-
        argMap.put("userProperties", new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
    }
    
    protected void launch(String[] args) throws Throwable {
        Class<GradleStartCommon> common = GradleStartCommon.class;
        System.setProperty("net.minecraftforge.gradle.GradleStart.srgDir", $.<File>$(common, "SRG_DIR").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg", $.<File>$(common, "SRG_NOTCH_SRG").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-mcp", $.<File>$(common, "SRG_NOTCH_MCP").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp", $.<File>$(common, "SRG_SRG_MCP").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-srg", $.<File>$(common, "SRG_MCP_SRG").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-notch", $.<File>$(common, "SRG_MCP_NOTCH").getCanonicalPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.csvDir", $.<File>$(common, "CSV_DIR").getCanonicalPath());
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
        String nativesDir = isEmptyOr(System.getProperty("index.alchemy.gradle.path"), "") +
                "/.gradle/caches/minecraft/net/minecraft/natives/" + MC_VERSION;
        paths = Strings.isNullOrEmpty(paths) ? nativesDir : paths + File.pathSeparator + nativesDir;
        System.setProperty("java.library.path", paths);
        $(ClassLoader.class, "sys_paths<", null);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Module base = Object.class.getModule();
        base.getPackages().forEach(packageName -> $(base, "implAddOpensToAllUnnamed", packageName));
        Object urlClassPath = $(classLoader, "ucp");
        List<URL> list = $(urlClassPath, "path");
        list.removeIf(url -> url.toString().contains("dlcs-bin"));
        Stack<URL> stack = $(urlClassPath, "urls");
        stack.removeIf(url -> url.toString().contains("dlcs-bin"));
    }
    
    private static final Attributes.Name FMLAT = new Attributes.Name("FMLAT"), FML_CORE_PLUGIN = new Attributes.Name("FMLCorePlugin");
    
    public static void searchCoremods(GradleStartCommon common) throws Exception {
        List<String> extras = $(common, "extras");
        for (URL url : $.<URL[]>$($(GradleStartCommon.class.getClassLoader(), "ucp"), "getURLs")) {
            if (!url.getProtocol().startsWith("file"))
                continue;
            File coreMod = new File(url.toURI().getPath());
            if (!coreMod.exists())
                continue;
            Manifest manifest = null;
            if (coreMod.getName().endsWith("jar"))
                try (JarFile jar = new JarFile(coreMod)) {
                    if (jar.getManifest() != null) {
                        manifest = jar.getManifest();
                        String ats = manifest.getMainAttributes().getValue(FMLAT);
                        if (ats != null && !ats.isEmpty())
                            ModAccessTransformer.addJar(jar, ats);
                    }
                }
            if (manifest != null) {
                String clazz = manifest.getMainAttributes().getValue(FML_CORE_PLUGIN);
                if (!Strings.isNullOrEmpty(clazz)) {
                    LOGGER.info("Found and added coremod: " + clazz);
                    GradleForgeHacks.coreMap.put(clazz, coreMod);
                }
            }
        }
        HashSet<String> coremods = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(System.getProperty("fml.coreMods.load")))
            coremods.addAll(Splitter.on(',').splitToList(System.getProperty("fml.coreMods.load")));
        System.setProperty("fml.coreMods.load", Joiner.on(',').join(coremods));
        if (!Strings.isNullOrEmpty($(common, "getTweakClass"))) {
            extras.add("--tweakClass");
            extras.add("net.minecraftforge.gradle.tweakers.CoremodTweaker");
        }
    }
    
}
