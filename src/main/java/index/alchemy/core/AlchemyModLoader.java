package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import baubles.common.Baubles;
import index.alchemy.api.IDLCInfo;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Premise;
import index.alchemy.api.annotation.Test;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.core.AlchemyModLoader.*;
import static index.alchemy.util.FunctionHelper.*;

/* 
 * -----------------------------------------------
 *    __    __    ___  _   _  ____  __  __  _  _ 
 *   /__\  (  )  / __)( )_( )( ___)(  \/  )( \/ )
 *  /(__)\  )(__( (__  ) _ (  )__)  )    (  \  / 
 * (__)(__)(____)\___)(_) (_)(____)(_/\/\_) (__) 
 *                                               
 * -----------------------------------------------
 */

@Omega
@Loading
@Premise({ Baubles.MODID, BOP_ID })
@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = MOD_VERSION,
		dependencies = REQUIRED_AFTER + BOP_ID + "@[5.0.0,);" + REQUIRED_AFTER + "Forge@[12.18.3.2185,);after:*;"
)
public enum AlchemyModLoader {
	
	INSTANCE;
	
	@Mod.InstanceFactory
	public static AlchemyModLoader instance() { return INSTANCE; }
	
	public static final String REQUIRED_BEFORE = "required-before:", REQUIRED_AFTER = "required-after:";
	
	public static final Random random = new Random();
	
	public static final Logger logger = LogManager.getLogger(MOD_NAME);
	
	public static final Stack<String> log_stack = new Stack<String>();
	
	public static void updateStack(String prefix) {
		int index = log_stack.indexOf(prefix);
		if (log_stack.size() == 0 || index == -1)
			log_stack.push(prefix);
		else
			if (index != log_stack.size() - 1)
				for (int i = 0, len = log_stack.size() - 1 - index; i < len; i++)
					log_stack.pop();
	}
	
	public static void info(String prefix, String info) {
		updateStack(prefix);
		logger.info(Tool.makeString(' ', log_stack.size() * 4) + prefix + ": " + info);
	}
	
	public static void info(Class<?> clazz, Object obj) {
		info("Init", "<" + clazz.getName() + "> " + obj);
	}
	
	public static final String mc_dir;
	public static final boolean is_modding, enable_test, enable_dmain;
	public static final File mod_path;
	private static final Map<ModState, LinkedList<Class<?>>> init_map = new LinkedHashMap<ModState, LinkedList<Class<?>>>() {
		
		@Override
		public LinkedList<Class<?>> get(Object key) {
			LinkedList<Class<?>> result = super.get(key);
			if (result == null)
				put((ModState) key, result = Lists.newLinkedList());
			return result;
		}
		
	};
	private static final Map<String, LinkedList<Class<?>>> instance_map = new LinkedHashMap<String, LinkedList<Class<?>>>() {
		
		@Override
		public LinkedList<Class<?>> get(Object key) {
			LinkedList<Class<?>> result = super.get(key);
			if (result == null)
				put((String) key, result = Lists.newLinkedList());
			return result;
		}
		
	};
	private static final List<MethodHandle> loading_list = Lists.newLinkedList();
	private static final List<String> class_list = Lists.newLinkedList();
	
	private static final Map<Class<? extends FMLEvent>, List<Consumer<FMLEvent>>>
			fml_event_callback_mapping = new HashMap<Class<? extends FMLEvent>, List<Consumer<FMLEvent>>>() {
		
		@Override
		public List<Consumer<FMLEvent>> get(Object key) {
			List<Consumer<FMLEvent>> result = super.get(key);
			if (result == null)
				put((Class<? extends FMLEvent>) key, result = Lists.newLinkedList());
			return result;
		}
		
	};
	
	public static <T extends FMLEvent> void addFMLEventCallback(Class<T> clazz, Consumer<T> consumer) {
		fml_event_callback_mapping.get(clazz).add((Consumer<FMLEvent>) consumer);
	}
	
	public static <T extends FMLEvent> void onFMLEvent(T event) {
		fml_event_callback_mapping.get(event.getClass()).forEach(c -> c.accept(event)); 
	}
	
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
	
	public static boolean isModLoaded(String modid) {
		for (ModContainer modContainer : Loader.instance().getModList())
			if (modContainer.getModId().equals(modid))
				return true;
		return false;
	}
	
	public static void init(Class<?> clazz) throws IllegalAccessException, InstantiationException {
		DLC dlc = clazz.getAnnotation(DLC.class);
		if (dlc != null) {
			IDLCInfo info = AlchemyDLCLoader.findDLC(dlc.name());
			Object instance = clazz.newInstance();
			for (Method method : clazz.getMethods()) {
				if (!Modifier.isStatic(method.getModifiers()) && method.getReturnType() == void.class) {
					EventHandler handler = method.getAnnotation(EventHandler.class);
					if (handler != null) {
						Class<?> args[] = method.getParameterTypes();
						if (args.length == 1 && Tool.isInstance(FMLEvent.class, args[0])) {
							MethodHandle handle = AlchemyCorePlugin.lookup().unreflect(method).bindTo(instance);
							AlchemyModLoader.addFMLEventCallback((Class<FMLEvent>) args[0],
									onThrowable(e -> handle.invoke(e), AlchemyRuntimeException::onException));
						}
					}
				}
			}
			if (AlchemyCorePlugin.runtimeSide().isClient())
				FMLClientHandler.instance().addModAsResource(info.getDLCContainer());
		}
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
		} catch (IOException e) { AlchemyRuntimeException.onException(e); }
	}
	
	static {
		logger.info("Max Direct Memory: " + sun.misc.VM.maxDirectMemory());
		
		is_modding = !AlchemyCorePlugin.isRuntimeDeobfuscationEnabled();
		mc_dir = AlchemyCorePlugin.getMinecraftDir().getPath();
		if (AlchemyCorePlugin.getAlchemyCoreLocation() != null)
			mod_path = AlchemyCorePlugin.getAlchemyCoreLocation();
		else try {
			String offset = AlchemyModLoader.class.getName().replace('.', '/') + ".class";
			URL src = AlchemyModLoader.class.getResource("/" + offset);
			if (src.getProtocol().equals("jar"))
				mod_path = new File(((JarURLConnection) src.openConnection()).getJarFileURL().getFile());
			else if (src.getProtocol().equals("file"))
				mod_path = new File(src.getFile().replace(offset, ""));
			else {
				mod_path = null;
				throw new NullPointerException("mod_path");
			}
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			throw new RuntimeException(e);
		}
		logger.info("Mod path: " + mod_path);
		
		enable_test = Boolean.getBoolean("index.alchemy.enable_test");
		logger.info("Test mode state: " + enable_test);
		
		enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
		logger.info("Development mode state: " + enable_dmain);
	}
	
	private static final String BOOTSTRAP = "bootstrap";
	
	private static void bootstrap() throws Throwable {
		checkInvokePermissions();
		AlchemyTransformerManager.loadAllTransformClass();
		
		try {
			for (String line : Tool.read(AlchemyModLoader.class.getResourceAsStream("/ascii_art.txt")).split("\n"))
				logger.info(line);
		} catch (Exception e) { }
		
		AlchemyDebug.start(BOOTSTRAP);
		class_list.addAll(0, AlchemyCorePlugin.findClassFromURL(mod_path.toURI().toURL()));
		
		AlchemyDLCLoader.stream().map(IDLCInfo::getDLCAllClass).forEach(AlchemyModLoader::addClass);
		
		Side side = AlchemyCorePlugin.runtimeSide();
		ClassLoader loader = AlchemyCorePlugin.getLaunchClassLoader();
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				SideOnly only = clazz.getAnnotation(SideOnly.class);
				if (only != null && only.value() != side)
					continue;
				Loading loading = clazz.getAnnotation(Loading.class);
				if (loading != null) {
					logger.info(AlchemyModLoader.class.getName() + " Add -> " + clazz);
					loading_list.add(AlchemyCorePlugin.lookup().findStatic(clazz, "init", MethodType.methodType(void.class, Class.class)));
				}
			} catch (ClassNotFoundException e) { continue; }
		}
		
		for (String name : class_list) {
			try {
				Class<?> clazz = Class.forName(name, false, loader);
				logger.info(AlchemyModLoader.class.getName() + " Loading -> " + clazz);
				for (MethodHandle handle : loading_list)
					handle.invoke(clazz);
				Init init = clazz.getAnnotation(Init.class);
				if (init != null && init.enable())
					if (init.index() < 0)
						init_map.get(init.state()).addFirst(clazz);
					else
						init_map.get(init.state()).add(clazz);
				InitInstance instance = clazz.getAnnotation(InitInstance.class);
				if (instance != null)
					if (instance.value() != null)
						instance_map.get(instance.value()).add(clazz);
					else
						AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @InitInstance.value()"));
			} catch (ClassNotFoundException e) { continue; }
		}
		
		AlchemyDebug.end(BOOTSTRAP);
		log_stack.clear();
		
		init(ModState.LOADED);
		AlchemyUpdateManager.invoke(MOD_ID, DEV_VERSION, null, new File(mod_path.toURI()));
	}
	
	public static String format(String src, String max) {
		double fix = (max.length() - src.length()) / 2D;
		return Tool.getString(' ', (int) Math.floor(fix)) + src + Tool.getString(' ', (int) Math.ceil(fix));
	}
	
	private static void init(ModState state) {
		if (AlchemyModLoader.state.ordinal() >= state.ordinal())
			AlchemyRuntimeException.onException(new RuntimeException(
					"old state(" + getState().name() + ") > new state(" + state.name() + ")"));
		log_stack.clear();
		AlchemyModLoader.state = state;
		String state_str = format(state.toString(), ModState.POSTINITIALIZED.toString());
		logger.info("************************************   " + state_str + " START   ************************************");
		ProgressBar bar = ProgressManager.push("AlchemyModLoader", init_map.get(state).size());
		for (Class clazz : init_map.get(state)) {
			bar.step(clazz.getSimpleName());
			if (clazz.getAnnotation(Test.class) == null || enable_test)
				init0(clazz);
		}
		ProgressManager.pop(bar);
		logger.info("************************************   " + state_str + "  END    ************************************");
	}
	
	public static void init0(Class<?> clazz) {
		try {
			logger.info("Starting init class: " + clazz.getName());
			try {
				AlchemyCorePlugin.lookup().findStatic(clazz, "init", MethodType.methodType(void.class)).invoke();
			} catch (NoSuchMethodException e) {
				Tool.init(clazz);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
			logger.info("Successful !");
		} catch (Exception e) {
			logger.error("Failed !");
			AlchemyRuntimeException.onException(e);
		}
	}
	
	@EventHandler
	public void onFMLConstruction(FMLConstructionEvent event) {
		try { bootstrap(); } catch (Throwable e) { AlchemyRuntimeException.onException(new RuntimeException("Can't bootstrap !!!", e)); }
		init(ModState.CONSTRUCTED);
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLPreInitialization(FMLPreInitializationEvent event) {
		init(ModState.PREINITIALIZED);
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLInitialization(FMLInitializationEvent event) {
		init(ModState.INITIALIZED);
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLPostInitialization(FMLPostInitializationEvent event) {
		init(ModState.POSTINITIALIZED);
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLLoadComplete(FMLLoadCompleteEvent event) {
		init(ModState.AVAILABLE);
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLServerAboutToStart(FMLServerAboutToStartEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLServerStarting(FMLServerStartingEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLServerStarted(FMLServerStartedEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLServerStopping(FMLServerStoppingEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLServerStopped(FMLServerStoppedEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLFingerprintViolation(FMLFingerprintViolationEvent event) {
		onFMLEvent(event);
	}

	@EventHandler
	public void onFMLMissingMappings(FMLMissingMappingsEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLModDisabled(FMLModDisabledEvent event) {
		onFMLEvent(event);
	}
	
	@EventHandler
	public void onFMLModIdMapping(FMLModIdMappingEvent event) {
		onFMLEvent(event);
	}
	
}
