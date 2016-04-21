package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import index.alchemy.api.Alway;
import index.alchemy.block.AlchemyBlockLoader;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.development.DMain;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.potion.AlchemyPotionLoader;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Constants.MODID, version = Constants.MOD_VERSION)
public class AlchemyModLoader {
	
	public static final Logger logger = LogManager.getLogger(Constants.MODID);
	
	public static URLClassLoader loader;
	
	@Instance(Constants.MODID)
	public static AlchemyModLoader instance;
	
	@SidedProxy(clientSide = Constants.MOD_PACKAGE + ".client.ClientProxy", serverSide = Constants.MOD_PACKAGE + ".core.CommonProxy")
	public static CommonProxy proxy;
	
	private AlchemyEventSystem event_system;
	private AlchemyConfigLoader config;
	
	public AlchemyEventSystem getEventSystem() {
		return event_system;
	}
	
	public AlchemyConfigLoader getConfig() {
		return config;
	}
	
	public AlchemyModLoader() {
		if (instance != null)
			throw new RuntimeException("Before this has been instantiate.");
	}
	
	public static final String mc_dir; 
	public static final boolean is_modding;
	public static Map<ModState, List<Class<?>>> init_map = new LinkedHashMap<ModState, List<Class<?>>>();
	
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
					if (is_modding)
						DMain.init(clazz);
					for (Init init : clazz.getAnnotationsByType(Init.class)) {
						SideOnly[] side = clazz.getAnnotationsByType(SideOnly.class);
						if (side.length > 0 && Alway.getSide() != side[0].value())
							break;
						List<Class<?>> list = init_map.get(init.state());
						if (list == null)
							init_map.put(init.state(), list = new LinkedList<Class<?>>());
						list.add(clazz);
					}
				} catch (ClassNotFoundException e) {}
		}
		
	}
	
	public static void init(ModState state) {
		ProgressBar bar = ProgressManager.push("AlchemyModLoader", init_map.get(state).size());
		logger.info("************************************   " + state + " START   ************************************");
		for (Class clazz : init_map.get(state)) {
			bar.step(clazz.getSimpleName());
			init(clazz);
		}
		logger.info("************************************   " + state + "  END    ************************************");
		ProgressManager.pop(bar);
	}
	
	public static void init(Class<?> clazz) {
		try {
			logger.info("Starting init class: " + clazz.getName());
			clazz.getMethod("init").invoke(null);
			logger.info("Successful !");
		} catch (Exception e) {
			logger.error("Failed !");
			throw new AlchemyRuntimeExcption(e);
		}
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws MalformedURLException {
		loader = new URLClassLoader(new URL[]{event.getSourceFile().toURI().toURL()});
		event_system = new AlchemyEventSystem(this);
		config = new AlchemyConfigLoader(event.getSuggestedConfigurationFile());
		init(event.getModState());
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		init(event.getModState());
		init(ModState.AVAILABLE);
	}
	
}
