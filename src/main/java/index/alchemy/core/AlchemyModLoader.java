package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import index.alchemy.api.Alway;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Test;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;

@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = MOD_VERSION,
		dependencies = "required-after:" + BOP_ID
)
public class AlchemyModLoader {
	
	public static final Logger logger = LogManager.getLogger(MOD_ID);
	
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
	}
	
	@Nullable
	@Deprecated
	@SidedProxy(clientSide = MOD_PACKAGE + ".client.ClientProxy", serverSide = MOD_PACKAGE + ".core.CommonProxy")
	private static CommonProxy proxy;
	
	public static CommonProxy getProxy() {
		if (proxy == null)
			AlchemyRuntimeException.onException(new NullPointerException(AlchemyModLoader.class.getName() + ".proxy"));
		return proxy;
	}
	
	public static final String mc_dir, mod_path;
	public static final boolean is_modding, enable_test, enable_dmain;
	public static final Map<ModState, List<Class<?>>> init_map = new LinkedHashMap<ModState, List<Class<?>>>() {
		@Override
		public List<Class<?>> get(Object key) {
			List<Class<?>> result = super.get(key);
			if (result == null)
				put((ModState) key, result = new LinkedList());
			return result;
		}
	};
	public static final Map<String, List<Class<?>>> instance_map = new LinkedHashMap<String, List<Class<?>>>() {
		@Override
		public List<Class<?>> get(Object key) {
			List<Class<?>> result = super.get(key);
			if (result == null)
				put((String) key, result = new LinkedList());
			return result;
		}
	};
	public static final List<Method> loading_list = new LinkedList<Method>();
	
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
	
	public static void deleteOnExit() {
		checkInvokePermissions();
		new File(mod_path).deleteOnExit();
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
		String str = AlchemyModLoader.class.getResource("/alchemy.info").toString()
				.replace("file:/", "").replace("\\", "/")
				.replace("/bin/alchemy.info", "");
		
		if (!str.contains("alchemy.info")) {
			mod_path = Tool.decode(str + "/bin/");
			mc_dir = str;
			is_modding = true;
		} else {
			mod_path = Tool.decode(str.replace("\\", "/").replace("!/alchemy.info", "").replace("jar:", ""));
			mc_dir =  str.replaceAll("/mods/.*?jar!.*", "").replace("jar:", "");
			is_modding = false;
		}
		
		enable_test = Boolean.getBoolean("index.alchemy.enable_test");
		logger.info("Test mode state: " + enable_test);
		
		enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
		logger.info("Development mode state: " + enable_dmain);
		
		List<String> class_list = new LinkedList<String>();
		
		boolean flag = true;
		try {
			flag = Tool.setAccessible(Loader.class.getDeclaredField("instance")).get(null) != null;
		} catch (Exception e) {
			logger.warn(e);
		}
		
		if (flag) {
			if (is_modding) {
				List<String> temp = new LinkedList<String>();
				Tool.getAllFile(new File(mod_path + AlchemyConstants.MOD_PACKAGE.replace('.', '/')), temp);
				for (String name : temp)
					if (name.endsWith(".class"))
						class_list.add(name.replace("\\", "/").replace(mod_path, "")
								.replace(".class", "").replace("/", "."));
			} else {
				JarFile jar = null;
				try {
					jar = new JarFile(new File(mod_path));
					Enumeration<JarEntry> entry = jar.entries();
					while (entry.hasMoreElements()) {
						String name = entry.nextElement().getName();
						if (name.endsWith(".class"))
							class_list.add(name.replace(".class", "").replace("/", "."));
					}
				} catch (IOException e) {
					AlchemyRuntimeException.onException(e);
				} finally {
					if (jar != null)
						try {
							jar.close();
						} catch (IOException e) {}
				}
			}
			
			for (String name : new LinkedList<String>(class_list))
				if (name.matches(".*\\$[0-9]+"))
					class_list.remove(name);
				
			
			Side side = Alway.getSide();
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
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
			}
			
			for (String name : class_list) {
				try {
					Class<?> clazz = Class.forName(name, false, loader);
					logger.info(AlchemyModLoader.class.getName() + " Loading -> " + clazz);
					SideOnly only = clazz.getAnnotation(SideOnly.class);
					if (only != null && only.value() != side)
						continue;
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
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
			}
			
			init(ModState.UNLOADED);
			AlchemyUpdateManager.invoke(MOD_ID);
			
		}
	}
	
	public static String format(String src, String max) {
		double fix = (max.length() - src.length()) / 2D;
		return Tool.getString(' ', (int) Math.floor(fix)) + src + Tool.getString(' ', (int) Math.ceil(fix));
	}
	
	private static void init(ModState state) {
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
