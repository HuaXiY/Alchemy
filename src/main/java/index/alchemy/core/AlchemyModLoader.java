package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import baubles.common.Baubles;
import index.alchemy.api.Always;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Premise;
import index.alchemy.api.annotation.Test;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.core.AlchemyModLoader.*;

/* 
 * -----------------------------------------------
 *    __    __    ___  _   _  ____  __  __  _  _ 
 *   /__\  (  )  / __)( )_( )( ___)(  \/  )( \/ )
 *  /(__)\  )(__( (__  ) _ (  )__)  )    (  \  / 
 * (__)(__)(____)\___)(_) (_)(____)(_/\/\_) (__) 
 * 
 * -----------------------------------------------
 */

@Premise({ Baubles.MODID, BOP_ID })
@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = MOD_VERSION,
		dependencies = REQUIRED_AFTER + BOP_ID
)
public class AlchemyModLoader {
	
	public static final String REQUIRED_BEFORE = "required-before:", REQUIRED_AFTER = "required-after:";
	
	public static final Logger logger = LogManager.getLogger(MOD_NAME);
	
	public static final Random random = new Random();
	
	@Nullable
	@Deprecated
	@Instance(MOD_ID)
	private static AlchemyModLoader instance;
	
	public static Object instance() {
		if (instance == null)
			AlchemyRuntimeException.onException(new NullPointerException(AlchemyModLoader.class.getName() + ".instance"));
		return instance;
	}
	
	public AlchemyModLoader() {
		if (instance != null)
			AlchemyRuntimeException.onException(new RuntimeException("Before this has been instantiate"));
		else 
			try {
				bootstrap();
			} catch (Exception e) {
				AlchemyRuntimeException.onException(new RuntimeException("Can't bootstrap !!!", e));
			}
	}
	
	public static final String mc_dir, mod_path;
	public static final boolean is_modding, enable_test, enable_dmain;
	private static final Map<ModState, List<Class<?>>> init_map = new LinkedHashMap<ModState, List<Class<?>>>() {
		@Override
		public List<Class<?>> get(Object key) {
			List<Class<?>> result = super.get(key);
			if (result == null)
				put((ModState) key, result = new LinkedList());
			return result;
		}
	};
	private static final Map<String, List<Class<?>>> instance_map = new LinkedHashMap<String, List<Class<?>>>() {
		@Override
		public List<Class<?>> get(Object key) {
			List<Class<?>> result = super.get(key);
			if (result == null)
				put((String) key, result = new LinkedList());
			return result;
		}
	};
	private static final List<Method> loading_list = new LinkedList<Method>();
	private static final List<String> class_list = new LinkedList<String>();
	
	public static List<Class<?>> getInstance(String key) {
		return instance_map.get(key);
	}
	
	public static void addClass(List<String> classes) {
		checkInvokePermissions();
		checkState();
		for (String clazz : classes)
			if (class_list.contains(clazz))
				AlchemyRuntimeException.onException(new RuntimeException());
			else
				class_list.add(clazz);
	}
	
	private static ModState state = ModState.UNLOADED;
	
	public static ModState getState() {
		return state;
	}
	
	public static boolean isAvailable() {
		return getState().ordinal() >= ModState.AVAILABLE.ordinal();
	}
	
	public static void checkState() {
		if (isAvailable())
			AlchemyRuntimeException.onException(new RuntimeException("Abnormal state: " + getState().name()));
	}
	
	public static void checkState(ModState state) {
		if (getState() != state)
			AlchemyRuntimeException.onException(new RuntimeException("Abnormal state: " + getState().name()));
	}
	
	public static void checkInvokePermissions() {
		Tool.checkInvokePermissions(3, AlchemyModLoader.class);
	}
	
	public static void restart() {
		checkInvokePermissions();
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String cp = bean.getClassPath();
		List<String> args = bean.getInputArguments();
		String main = System.getProperty("sun.java.command");
		try {
			Process process = Runtime.getRuntime().exec("java " + Joiner.on(' ').join(args) + " -cp " + cp + " " + main);
			FMLCommonHandler.instance().exitJava(0x0, false);
		} catch (IOException e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	static {
		try {
			for (String line : Tool.read(AlchemyModLoader.class.getResourceAsStream("/ascii_art.txt")).split("\n"))
				logger.info(line);
		} catch (Exception e) {}
		
		is_modding = AlchemyModLoader.class.getResource("/alchemy.info").getProtocol().equals("file");
		mod_path = AlchemyModLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath()
				.replace(AlchemyModLoader.class.getName().replace('.', '/') + ".class", "");
		mc_dir = System.getProperty("user.dir").replace('\\', '/');
		
		enable_test = Boolean.getBoolean("index.alchemy.enable_test");
		logger.info("Test mode state: " + enable_test);
		
		enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
		logger.info("Development mode state: " + enable_dmain);
	}
	
	public static List<String> findClassFromURL(URL url) throws Exception {
		List<String> result = new LinkedList<String>();
		ClassLoader loader = new URLClassLoader(new URL[]{ url }, null);
		ClassPath path = ClassPath.from(loader);
		for (ClassInfo info : path.getAllClasses())
			if (!info.getName().matches(".*\\$[0-9]+"))
				result.add(info.getName());
		return result;
	}
	
	private static void bootstrap() throws Exception {
		AlchemyDebug.start("bootstrap");
		URL url = new File(mod_path).toURI().toURL();
		class_list.addAll(findClassFromURL(url));
		
		AlchemyDLCLoader.setup();
		
		Side side = Always.getSide();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				SideOnly only = clazz.getAnnotation(SideOnly.class);
				if (only != null && only.value() != side)
					continue;
				Loading loading = clazz.getAnnotation(Loading.class);
				if (loading != null) {
					loading_list.add(clazz.getMethod("init", Class.class));
					logger.info(AlchemyModLoader.class.getName() + " Add -> " + clazz);
				}
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				logger.info(AlchemyModLoader.class.getName() + " Loading -> " + clazz);
				for (Method method : loading_list)
					method.invoke(null, clazz);
				Init init = clazz.getAnnotation(Init.class);
				if (init != null && init.enable())
					init_map.get(init.state()).add(clazz);
				InitInstance instance = clazz.getAnnotation(InitInstance.class);
				if (instance != null)
					if (instance.value() != null)
						instance_map.get(instance.value()).add(clazz);
					else
						AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @InitInstance.value()"));
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		AlchemyDebug.end("bootstrap");
		
		init(ModState.LOADED);
		AlchemyUpdateManager.invoke(MOD_ID, DEV_VERSION, null, new File(mod_path));
	}
	
	public static String format(String src, String max) {
		double fix = (max.length() - src.length()) / 2D;
		return Tool.getString(' ', (int) Math.floor(fix)) + src + Tool.getString(' ', (int) Math.ceil(fix));
	}
	
	private static void init(ModState state) {
		if (AlchemyModLoader.state.ordinal() >= state.ordinal())
			AlchemyRuntimeException.onException(new RuntimeException(
					"old state(" + getState().name() + ") > new state(" + state.name() + ")"));
		AlchemyModLoader.state = state;
		String state_str = format(state.toString(), ModState.POSTINITIALIZED.toString());
		logger.info("************************************   " + state_str + " START   ************************************");
		ProgressBar bar = ProgressManager.push("AlchemyModLoader", init_map.get(state).size());
		for (Class clazz : init_map.get(state)) {
			bar.step(clazz.getSimpleName());
			if (clazz.getAnnotation(Test.class) != null) {
				if (enable_test)
					Tool.init(clazz);
			} else
				init(clazz);
		}
		ProgressManager.pop(bar);
		logger.info("************************************   " + state_str + "  END    ************************************");
	}
	
	public static void init(Class<?> clazz) {
		try {
			logger.info("Starting init class: " + clazz.getName());
			Method method = null;
			try {
				method = clazz.getMethod("init");
			} catch (NoSuchMethodException e) {}
			if (method != null)
				method.invoke(null);
			else
				Tool.init(clazz);
			logger.info("Successful !");
		} catch (Exception e) {
			logger.error("Failed !");
			init(ModState.ERRORED);
			AlchemyRuntimeException.onException(e);
		}
	}
	
	@EventHandler
	public void onFMLConstruction(FMLConstructionEvent event) {
		init(ModState.CONSTRUCTED);
	}
	
	@EventHandler
	public void onFMLPreInitialization(FMLPreInitializationEvent event) {
		init(ModState.PREINITIALIZED);
	}
	
	@EventHandler
	public void onFMLInitialization(FMLInitializationEvent event) {
		init(ModState.INITIALIZED);
	}
	
	@EventHandler
	public void onFMLPostInitialization(FMLPostInitializationEvent event) {
		init(ModState.POSTINITIALIZED);
	}
	
	@EventHandler
	public void onFMLLoadComplete(FMLLoadCompleteEvent event) {
		init(ModState.AVAILABLE);
	}
	
}
