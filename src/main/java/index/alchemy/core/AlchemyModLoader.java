package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;

import index.alchemy.annotation.Init;
import index.alchemy.annotation.InitInstance;
import index.alchemy.api.Alway;
import index.alchemy.config.AlchemyConfigLoader;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.development.DMain;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION, dependencies = "required-after:BiomesOPlenty")
public class AlchemyModLoader {
	
	public static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
	
	@Nullable
	@Deprecated
	@Instance(Constants.MOD_ID)
	private static AlchemyModLoader instance;
	
	public static Object instance() {
		if (instance != null)
			return instance;
		throw new AlchemyRuntimeExcption(new NullPointerException("index.alchemy.core.AlchemyModLoader.instance"));
	}
	
	@SidedProxy(clientSide = Constants.MOD_PACKAGE + ".client.ClientProxy", serverSide = Constants.MOD_PACKAGE + ".core.CommonProxy")
	public static CommonProxy proxy;
	
	public AlchemyModLoader() {
		if (instance != null)
			throw new AlchemyRuntimeExcption(new RuntimeException("Before this has been instantiate"));
	}
	
	public static final String mc_dir; 
	public static final boolean is_modding, use_dmain;
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
	
	private static ModState state = ModState.UNLOADED;
	
	public static ModState getState() {
		return state;
	}
	
	public static void checkState() {
		if (getState().ordinal() >= ModState.AVAILABLE.ordinal())
			throw new AlchemyRuntimeExcption(new RuntimeException("Abnormal state: " + getState().name()));
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
		
		use_dmain = is_modding && Boolean.getBoolean(System.getProperty("index.alchemy.use_dmain", "false"));
		
		List<String> class_list = new LinkedList<String>();
		
		try {
			mod_path = URLDecoder.decode(mod_path, "utf-8");
		} catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		
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
				throw new AlchemyRuntimeExcption(e);
			} finally {
				if (jar != null)
					try {
						jar.close();
					} catch (IOException e) {}
			}
		}
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		for (String name : class_list) {
			if (name.startsWith("index."))
				try {
					Class<?> clazz = Class.forName(name, false, loader);
					if (use_dmain)
						DMain.init(clazz);
					AlchemyConfigLoader.init(clazz);
					AlchemyNetworkHandler.init(clazz);
					Init init = clazz.getAnnotation(Init.class);
					SideOnly side = clazz.getAnnotation(SideOnly.class);
					if (init != null && init.enable() && (side == null || Alway.getSide() == side.value()))
						init_map.get(init.state()).add(clazz);
					InitInstance instance = clazz.getAnnotation(InitInstance.class);
					if (instance != null)
						instance_map.get(instance.value()).add(clazz);
				} catch (ClassNotFoundException e) {}
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
			init(clazz);
		}
		ProgressManager.pop(bar);
		logger.info("************************************   " + state_str + "  END    ************************************");
	}
	
	public static void init(Class<?> clazz) {
		try {
			logger.info("Starting init class: " + clazz.getName());
			clazz.getMethod("init").invoke(null);
			logger.info("Successful !");
		} catch (Exception e) {
			logger.error("Failed !");
			init(ModState.ERRORED);
			throw new AlchemyRuntimeExcption(e);
		}
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws LWJGLException {
		init(ModState.CONSTRUCTED);
		init(ModState.PREINITIALIZED);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		init(ModState.INITIALIZED);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		init(ModState.POSTINITIALIZED);
		init(ModState.AVAILABLE);
	}
	
}
