package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import baubles.common.Baubles;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Premise;
import index.alchemy.api.annotation.Test;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
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

import static org.objectweb.asm.Opcodes.*;

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

@Omega
@Premise({ Baubles.MODID, BOP_ID })
@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = MOD_VERSION,
		dependencies = REQUIRED_AFTER + BOP_ID + ";" + REQUIRED_AFTER + "Forge@[12.18.3.2185,);after:*;"
)
public enum AlchemyModLoader {
	
	INSTANCE;
	
	@Mod.InstanceFactory
	public static AlchemyModLoader instance() {
		return INSTANCE;
	}
	
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
	
	public static class ASMClassLoader extends ClassLoader {
		
		private static final String HANDLER_DESC = Type.getInternalName(Function.class);
		private static final String HANDLER_FUNC_NAME = Function.class.getDeclaredMethods()[1].getName();
		private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(Function.class.getDeclaredMethods()[1]);
		
		private static int id = -1;
		
		public static synchronized int nextId() {
			return ++id;
		}
		
		private ASMClassLoader() {
			super(ASMClassLoader.class.getClassLoader());
		}
		
		public Class<?> define(String name, byte[] data) {
			return defineClass(name, data, 0, data.length);
		}
		
		private String getUniqueName(Method callback) {
			return String.format(
					"%s_%d_%s_%s_%s",
					getClass().getName(), nextId(),
					callback.getDeclaringClass().getSimpleName().replace("[]", "_L"),
					callback.getName(),
					callback.getParameterTypes()[0].getSimpleName().replace("[]", "_L")
			);
		}
		
		@Nullable
		@Unsafe(Unsafe.ASM_API)
		public Function createWrapper(Method callback, Object target) {
			Function result = null;
			
			ClassWriter cw = new ClassWriter(0);
			MethodVisitor mv;
			
			boolean isStatic = Modifier.isStatic(callback.getModifiers());
			String name = getUniqueName(callback);
			String desc = name.replace('.',  '/');
			String instType = Type.getInternalName(callback.getDeclaringClass());
			String callType = Type.getInternalName(callback.getParameterTypes()[0]);
			String handleName = callback.getName();
			String handleDesc = Type.getMethodDescriptor(callback);
			
			cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, desc, null, "java/lang/Object", new String[]{ HANDLER_DESC });
			cw.visitSource("AlchemyModLoader.java:159", "invoke: " + instType + handleName + handleDesc);
			{
				if (!isStatic)
					cw.visitField(ACC_PUBLIC | ACC_SYNTHETIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", isStatic ? "()V" : "(Ljava/lang/Object;)V", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
				if (!isStatic) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
				}
				mv.visitInsn(RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, HANDLER_FUNC_NAME, HANDLER_FUNC_DESC, null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				if (!isStatic) {
					mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
					mv.visitTypeInsn(CHECKCAST, instType);
				}
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, callType);
				mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, instType, handleName, handleDesc, false);
				Class<?> returnType = callback.getReturnType(), pack = Tool.getPrimitiveMapping(returnType);
				if (returnType == void.class)
					mv.visitInsn(ACONST_NULL);
				else if (returnType.isPrimitive())
					mv.visitMethodInsn(INVOKEVIRTUAL, ASMHelper.getClassDesc(pack),
							"valueOf", Type.getMethodDescriptor(Type.getType(pack), Type.getType(returnType)), false);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			cw.visitEnd();
			
			try {
				info("Define", name);
				Class<?> ret = define(name, cw.toByteArray());
				if (isStatic)
					result = (Function) ret.newInstance();
				else
					result = (Function) ret.getConstructor(Object.class).newInstance(target);
			} catch(Exception e) { AlchemyRuntimeException.onException(e); }
			return result;
		}
		
	}
	
	public static final ASMClassLoader asm_loader = new ASMClassLoader();
	
	public static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
	
	public static final String mc_dir, mod_path;
	public static final boolean is_modding, enable_test, enable_dmain;
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
		logger.info("Max Direct Memory: " + sun.misc.VM.maxDirectMemory());
		
		is_modding = !AlchemyCorePlugin.isRuntimeDeobfuscationEnabled();
		mod_path = AlchemyModLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath()
				.replace(ASMHelper.getClassName(AlchemyModLoader.class) + ".class", "");
		mc_dir = AlchemyCorePlugin.getMinecraftDir().getPath();
		
		enable_test = Boolean.getBoolean("index.alchemy.enable_test");
		logger.info("Test mode state: " + enable_test);
		
		enable_dmain = is_modding && Boolean.getBoolean("index.alchemy.enable_dmain");
		logger.info("Development mode state: " + enable_dmain);
	}
	
	public static List<String> findClassFromURL(URL url) throws Exception {
		List<String> result = Lists.newLinkedList();
		ClassLoader loader = new URLClassLoader(new URL[]{ url }, null);
		ClassPath path = ClassPath.from(loader);
		for (ClassInfo info : path.getAllClasses())
			if (!info.getName().matches(".*\\$[0-9]+") && !info.getName().contains("$$"))
				result.add(info.getName());
		return result;
	}
	
	private static void bootstrap() throws Throwable {
		checkInvokePermissions();
		AlchemyTransformerManager.loadAllTransformClass();
		
		try {
			for (String line : Tool.read(AlchemyModLoader.class.getResourceAsStream("/ascii_art.txt")).split("\n"))
				logger.info(line);
		} catch (Exception e) {}
		
		AlchemyDebug.start("bootstrap");
		URL url = new File(mod_path).toURI().toURL();
		class_list.addAll(0, findClassFromURL(url));
		
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
					logger.info(AlchemyModLoader.class.getName() + " Add -> " + clazz);
					loading_list.add(lookup.findStatic(clazz, "init", MethodType.methodType(void.class, Class.class)));
				}
			} catch (ClassNotFoundException e) {
				continue;
			}
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
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		
		AlchemyDebug.end("bootstrap");
		log_stack.clear();
		
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
		log_stack.clear();
		AlchemyModLoader.state = state;
		String state_str = format(state.toString(), ModState.POSTINITIALIZED.toString());
		logger.info("************************************   " + state_str + " START   ************************************");
		ProgressBar bar = ProgressManager.push("AlchemyModLoader", init_map.get(state).size());
		for (Class clazz : init_map.get(state)) {
			bar.step(clazz.getSimpleName());
			if (clazz.getAnnotation(Test.class) == null || enable_test)
				init(clazz);
		}
		ProgressManager.pop(bar);
		logger.info("************************************   " + state_str + "  END    ************************************");
	}
	
	public static void init(Class<?> clazz) {
		try {
			logger.info("Starting init class: " + clazz.getName());
			try {
				lookup.findStatic(clazz, "init", MethodType.methodType(void.class)).invoke();
			} catch (NoSuchMethodException e) {
				Tool.init(clazz);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
			logger.info("Successful !");
		} catch (Exception e) {
			logger.error("Failed !");
			init(ModState.ERRORED);
			AlchemyRuntimeException.onException(e);
		}
	}
	
	@EventHandler
	public void onFMLConstruction(FMLConstructionEvent event) {
		try { bootstrap(); } catch (Throwable e) { throw new RuntimeException("Can't bootstrap !!!", e); }
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
