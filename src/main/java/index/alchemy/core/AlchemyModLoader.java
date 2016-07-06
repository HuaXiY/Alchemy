package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URLDecoder;
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
import index.alchemy.development.DMain;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION, dependencies = "required-after:BiomesOPlenty")
public class AlchemyModLoader {
	
	public static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
	
	public static final Random random = new Random();
	
	@Nullable
	@Deprecated
	@Instance(Constants.MOD_ID)
	private static AlchemyModLoader instance;
	
	public static Object instance() {
		if (instance == null)
			AlchemyRuntimeException.onException(new NullPointerException("index.alchemy.core.AlchemyModLoader.instance"));
		return instance;
	}
	
	public AlchemyModLoader() {
		if (instance != null)
			AlchemyRuntimeException.onException(new RuntimeException("Before this has been instantiate"));
	}
	
	@SidedProxy(clientSide = Constants.MOD_PACKAGE + ".client.ClientProxy", serverSide = Constants.MOD_PACKAGE + ".core.CommonProxy")
	private static CommonProxy proxy;
	
	public static CommonProxy getProxy() {
		return proxy;
	}
	
	public static final String mc_dir; 
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
	
	public static void restart() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String cp = bean.getClassPath();
		List<String> args = bean.getInputArguments();
		String main = System.getProperty("sun.java.command");
		try {
			Process process = Runtime.getRuntime().exec("java " + Joiner.on(' ').join(args) + " -cp " + cp + " " + main);
			FMLCommonHandler.instance().exitJava(0xC001, false);
		} catch (IOException e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	static {
		String str = AlchemyModLoader.class.getResource("/alchemy.info").toString()
				.replace("file:/", "").replace("\\", "/")
				.replace("/bin/alchemy.info", ""), mod_path;
		
		if (!str.contains("alchemy.info")) {
			mod_path = str + "/bin/";
			mc_dir = str;
			is_modding = true;
		} else {
			mod_path = str.replace("\\", "/").replace("!/alchemy.info", "").replace("jar:", "");
			mc_dir =  str.replaceAll("/mods/.*?jar!.*", "").replace("jar:", "");
			is_modding = false;
		}
		
		enable_test = Boolean.getBoolean("index.alchemy.enable_test");
		logger.info("Test mode state: " + enable_test);
		
		enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
		logger.info("Development mode state: " + enable_dmain);
		
		List<String> class_list = new LinkedList<String>();
		
		try {
			mod_path = URLDecoder.decode(mod_path, "utf-8");
		} catch (UnsupportedEncodingException e) {
			AlchemyRuntimeException.onException(e);
		}
		
		if (is_modding) {
			List<String> temp = new LinkedList<String>();
			Tool.getAllFile(new File(mod_path + Constants.MOD_PACKAGE.replace('.', '/')), temp);
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
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				Loading loading = clazz.getAnnotation(Loading.class);
				if (loading != null) {
					loading_list.add(clazz.getMethod("init", Class.class));
					logger.info(AlchemyModLoader.class.getName() + " Add -> " + clazz);
				}
			} catch (ClassNotFoundException e) {} 
			catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
		}
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				logger.info(AlchemyModLoader.class.getName() + " Loading -> " + clazz);
				if (enable_dmain)
					DMain.init(clazz);
				for (Method method : loading_list)
					method.invoke(null, clazz);
				Init init = clazz.getAnnotation(Init.class);
				SideOnly side = clazz.getAnnotation(SideOnly.class);
				if (init != null && init.enable() && (side == null || Alway.getSide() == side.value()))
					init_map.get(init.state()).add(clazz);
				InitInstance instance = clazz.getAnnotation(InitInstance.class);
				if (instance != null)
					if (instance.value() != null)
						instance_map.get(instance.value()).add(clazz);
					else
						AlchemyRuntimeException.onException(new RuntimeException(new NullPointerException(clazz + " -> @InitInstance.value()")));
			} catch (ClassNotFoundException e) {}
			catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
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
					try {
						AlchemyInitHook.init(clazz.newInstance());
					} catch (Exception e) {
						AlchemyRuntimeException.onException(e);
					}
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
				Class.forName(clazz.getName());
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
