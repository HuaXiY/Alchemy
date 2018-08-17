package index.alchemy.core;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.agent.support.AgentSupport;
import index.alchemy.api.annotation.Alchemy;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.SuppressFBWarnings;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.asm.transformer.MeowTweaker;
import index.alchemy.core.asm.transformer.SrgMap;
import index.alchemy.core.asm.transformer.TransformerReplace;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.$;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.alchemy.util.FunctionHelper;
import index.alchemy.util.JFXHelper;
import index.alchemy.util.ModuleHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

@Omega
@Hook.Provider
@Name(CORE_MOD_ID)
@MCVersion(MC_VERSION)
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions(MOD_TRANSFORMER_PACKAGE)
public class AlchemyEngine extends $ implements IFMLLoadingPlugin {
	
	@FunctionalInterface
	public static interface IClassFileTransformer extends ClassFileTransformer {
		
		Set<Thread> THREADS = Sets.newHashSet();
		
		@Override
		default byte[] transform(ClassLoader loader, String className, @Nullable Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			return transform(null, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		}
		
		@Override
		default byte[] transform(Module module, ClassLoader loader, String className, @Nullable Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			return doTransform(module, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		}
		
		byte[] doTransform(@Nullable Module module, ClassLoader loader, String className, @Nullable Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
		
		static IClassFileTransformer of(IClassFileTransformer transformer) { return transformer; }
		
	}
	
	private static final String SETUP_CORE = "setup core", SRG_MCP = "net.minecraftforge.gradle.GradleStart.srg.srg-mcp";
	
	public static final double JAVA_VERSION = Optional.of(System.getProperty("java.specification.version")).map(Double::parseDouble).get();
	
	public static final PrintStream
			sysout = new PrintStream(new FileOutputStream(FileDescriptor.out)),
			syserr = new PrintStream(new FileOutputStream(FileDescriptor.err));
	
	public static final Logger logger = LogManager.getLogger(AlchemyEngine.class.getSimpleName());
	
	private static final sun.misc.Unsafe unsafe = FunctionHelper.onThrowableSupplier(AlchemyEngine::getUnsafe, FunctionHelper::rethrowVoid).get();
	
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
	
	public static final sun.misc.Unsafe unsafe() { return unsafe; }
	
	static { markUnsafe(unsafe()); }
	
	private static final MethodHandles.Lookup lookup = $(MethodHandles.Lookup.class, "new", Object.class, -1);
	
	public static MethodHandles.Lookup lookup() { return lookup; }
	
	static { ModuleHelper.openAllModule(); }
	
	public static LaunchClassLoader getLaunchClassLoader() { return Launch.classLoader; }

	public static Side runtimeSide() { return FMLLaunchHandler.side(); }
	
	protected static final class AgentLoader {
		
		public static void loadAgent() {
			logger.info("Loading agent ...");
			try {
				AgentSupport.loadAgent(Tool.createTempFile(AgentLoader.class.getResourceAsStream("/agent.jar"),
						WordUtils.initials("Index-Alchemy-Agent.", '-'), ".jar").getPath());
				logger.info("Successfully loaded agent!");
			} catch (Throwable t) {
				RuntimeException exception = new RuntimeException("Load agent failed!", t);
				logger.fatal(exception);
				throw exception;
			}
		}
		
	}
	
	protected static final java.lang.instrument.Instrumentation INSTRUMENTATION = null;
	
	public static final java.lang.instrument.Instrumentation instrumentation() { return INSTRUMENTATION; }
	
	protected static final MethodHandle runTransformers = FunctionHelper.onThrowableSupplier(() -> lookup().findVirtual(LaunchClassLoader.class,
			"runTransformers", MethodType.methodType(byte[].class, String.class, String.class, byte[].class)), FunctionHelper::rethrowVoid).get();
	
	public static byte[] runTransformers(String name, String transformedName, byte[] basicClass) throws Throwable {
		return (byte[]) runTransformers.invoke(getLaunchClassLoader(), name, transformedName, basicClass);
	}
	
	public static final void redefineClass(@Nonnull Class<?> target) throws Throwable {
		logger.info("Try to redefine the bytecodes: " + target.getName());
		instrumentation().redefineClasses(new ClassDefinition(target, ASMHelper.getClassData(target.getName())));
	}
	
	static {
		logger.info(AlchemyEngine.class.getName() + " loaded from classloader: " + AlchemyEngine.class.getClassLoader() );
		if ($(SystemUtils.class, "JAVA_SPECIFICATION_VERSION_AS_ENUM") == null)
			$(SystemUtils.class, "JAVA_SPECIFICATION_VERSION_AS_ENUM<", JavaVersion.JAVA_RECENT);
		if (AlchemyEngine.class.getClassLoader().getClass() == LaunchClassLoader.class) {
			logger.info("Try to redefine the bytecodes: " + LaunchClassLoader.class.getName());
			LaunchClassLoader lcl = (LaunchClassLoader) AlchemyEngine.class.getClassLoader();
			lcl.addClassLoaderExclusion("com.sun.");
			lcl.addClassLoaderExclusion("javafx.");
			AgentLoader.loadAgent();
			instrumentation().addTransformer(IClassFileTransformer.of((module, loader, name, target, domain, buffer) -> {
				try {
					if (target != null && name != null && !"net/minecraft/launchwrapper/LaunchClassLoader".equals(name)) {
						logger.info("Redefine: " + loader + "<" + target + ">" + (domain == null ? "(null)" : domain.getCodeSource()));
						buffer = runTransformers(ASMHelper.getClassSrcName(DeobfuscatingRemapper.instance().unmapType(name)),
								ASMHelper.getClassSrcName(name), buffer);
					}
					return buffer;
				} catch (Throwable t) {
					t.printStackTrace();
					throw new InternalError(t);
				}
			}), true);
			try {
				instrumentation().redefineClasses(new ClassDefinition(LaunchClassLoader.class, ASMHelper.ClassNameRemapper.changeName(
						lcl.getClassBytes("index.alchemy.core.AlchemyLaunchClassLoader"),
						"index.alchemy.core.AlchemyLaunchClassLoader",
						"net.minecraft.launchwrapper.LaunchClassLoader")));
				logger.info("Successfully redefine LaunchClassLoader!");
			} catch (Throwable t) {
				RuntimeException exception = new RuntimeException("Redefine LaunchClassLoader failed!", t);
				logger.fatal(t);
				throw exception;
			}
		}
		System.out.println("fixInstrumentation");
		String libArgs = System.getProperty("index.alchemy.runtime.lib.ext");
		Set<String> libs = libArgs != null ? Sets.newHashSet(Splitter.on(';').split(libArgs)) : Sets.newHashSet();
		// Forge hack native libs when startup
		libs.add("jfxrt");
		libs.forEach(AlchemyEngine::addRuntimeExtLibFromJRE);
	}
	
	static { /* JavaFX Tweaker */ MeowTweaker.Kyouko(); }
	
	private static boolean runtimeDeobfuscationEnabled = !Boolean.getBoolean("index.alchemy.runtime.deobf.disable");
	
	public static boolean isRuntimeDeobfuscationEnabled() { return runtimeDeobfuscationEnabled; }
	
	public static class ASMClassLoader extends ClassLoader {
		
		public static final String
				DYNAMIC_PACKAGE_NAME = System.getProperty("index.dynamic.define.package", "dynamic.define."),
				HANDLER_NAME = ASMHelper.getClassName("java.util.function.Function"),
				HANDLER_INSTANCE_NAME = "instance",
				HANDLER_NONSTAIC_INIT_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, ASMHelper.TYPE_OBJECT),
				HANDLER_FUNC_NAME = searchMethod(Function.class, "apply", Object.class).getName(),
				HANDLER_FUNC_DESC = Type.getMethodDescriptor(searchMethod(Function.class, "apply", Object.class));
		
		protected static final Logger logger = LogManager.getLogger(ASMClassLoader.class.getSimpleName());
		
		static { getLaunchClassLoader().addTransformerExclusion(DYNAMIC_PACKAGE_NAME); }
		
		private int id = -1;
		
		public synchronized int nextId() {
			return ++id;
		}
		
		private ASMClassLoader() {
			super(ASMClassLoader.class.getClassLoader());
		}
		
		public Class<?> define(String name, byte[] data) {
			String defineName = DYNAMIC_PACKAGE_NAME + name;
			logger.info(ASMClassLoader.class.getSimpleName() + " define: " + defineName);
			Class<?> result = defineClass(defineName, data = ASMHelper.ClassNameRemapper.changeName(data,
					ASMHelper.getClassName(name), ASMHelper.getClassName(defineName)), 0, data.length);
			Map<String, Class<?>> cachedClasses = $(getLaunchClassLoader(), "cachedClasses");
			cachedClasses.put(defineName, result);
			return result;
		}
		
		private String getUniqueName(java.lang.reflect.Method callback) {
			return String.format(
					"%s_%d_%s_%s_%s",
					getClass().getName(), nextId(),
					ASMHelper.getStdName(callback.getDeclaringClass().getSimpleName()),
					callback.getName(),
					ASMHelper.getStdName(callback.getParameterTypes()[0].getSimpleName())
			);
		}
		
		@Unsafe(Unsafe.ASM_API)
		public <T, R> Function<T, R> createWrapper(java.lang.reflect.Method callback, Object target) {
			ClassWriter cw = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			ASMHelper.MethodGenerator generator;
			boolean isStatic = Modifier.isStatic(callback.getModifiers()), isInterface = callback.getDeclaringClass().isInterface();
			String uniqueName = getUniqueName(callback),
					name = ASMHelper.getClassName(uniqueName),
					instName = callback.getDeclaringClass().getName(),
					callName = callback.getParameterTypes()[0].getName(),
					handleName = callback.getName(),
					handleDesc = Type.getMethodDescriptor(callback);
			Type owner = Type.getObjectType(name),
					instType = Type.getObjectType(ASMHelper.getClassName(instName)),
					callType = Type.getObjectType(ASMHelper.getClassName(callName));
			cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, name, null, ASMHelper.OBJECT_NAME, new String[]{ HANDLER_NAME });
			cw.visitSource("AlchemyEngine.java:303", "invoke: " + instName + handleName + handleDesc);
			{
				if (!isStatic)
					cw.visitField(ACC_PUBLIC | ACC_SYNTHETIC, HANDLER_INSTANCE_NAME, ASMHelper.OBJECT_DESC, null, null).visitEnd();
			}
			{
				generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, ASMHelper._INIT_,
						isStatic ? ASMHelper.VOID_METHOD_DESC : HANDLER_NONSTAIC_INIT_DESC, null, null);
				generator.loadThis();
				generator.invokeConstructor(ASMHelper.TYPE_OBJECT, new Method(ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC));
				if (!isStatic) {
					generator.loadThis();
					generator.loadArg(0);
					generator.putField(owner, HANDLER_INSTANCE_NAME, ASMHelper.TYPE_OBJECT);
				}
				generator.returnValue();
				generator.endMethod();
			}
			{
				generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, HANDLER_FUNC_NAME, HANDLER_FUNC_DESC, null, null);
				ASMHelper.MethodGenerator methodGenerator = generator;
				Runnable loadArgs = () -> {
					methodGenerator.loadThis();
					if (!isStatic) {
						methodGenerator.getField(owner, HANDLER_INSTANCE_NAME, ASMHelper.TYPE_OBJECT);
						if (Modifier.isPublic(callback.getDeclaringClass().getModifiers()))
							methodGenerator.checkCast(instType);
					}
					methodGenerator.loadArg(0);
					methodGenerator.checkCast(callType);
				};
				{
					Method method = new Method(handleName, handleDesc);
					if (Modifier.isPublic(callback.getModifiers()) && Modifier.isPublic(callback.getDeclaringClass().getModifiers())) {
						loadArgs.run();
						if (isStatic)
							generator.invokeStatic(instType, method);
						else if (isInterface)
							generator.invokeInterface(instType, method);
						else
							generator.invokeVirtual(instType, method);
					} else {
						FieldNode fieldNode;
						if (isStatic)
							fieldNode = generator.findStaticAndInvoke(owner, instType, method, loadArgs);
						else if (isInterface)
							fieldNode = generator.findInterfaceAndInvoke(owner, instType, method, loadArgs);
						else if (Modifier.isPrivate(callback.getModifiers()))
							fieldNode = generator.findSpecialAndInvoke(owner, instType, method, loadArgs);
						else
							fieldNode = generator.findVirtualAndInvoke(owner, instType, method, loadArgs);
						cw.visitField(fieldNode.access, fieldNode.name, fieldNode.desc, fieldNode.signature, fieldNode.value);
					}
				}
				Class<?> returnType = callback.getReturnType();
				if (returnType == void.class)
					generator.pushNull();
				else if (returnType.isPrimitive())
					generator.box(Type.getType(returnType));
				generator.returnValue();
				generator.endMethod();
			}
			cw.visitEnd();
			Class<?> ret = define(uniqueName, cw.toByteArray());
			return isStatic ? $(ret, "new") : $(ret, "new", target);
		}
		
		private String getUniqueName(MethodNode callback) {
			return String.format(
					"%s_%d_%s_%s",
					getClass().getName(), nextId(),
					callback.name,
					ASMHelper.getStdName(Type.getArgumentTypes(callback.desc)[0].getClassName())
			);
		}
		
		@Unsafe(Unsafe.ASM_API)
		public <T, R> Function<T, R> createWrapper(MethodNode callback) {
			ClassWriter cw = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS);
			ASMHelper.MethodGenerator generator;
			String uniqueName = getUniqueName(callback),
					name = ASMHelper.getClassName(uniqueName);
			cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, name, null, ASMHelper.OBJECT_NAME, new String[]{ HANDLER_NAME });
			cw.visitSource("AlchemyEngine.java:386", "invoke: " + callback.name + callback.desc);
			{
				generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, ASMHelper._INIT_,
						ASMHelper.VOID_METHOD_DESC, null, null);
				generator.loadThis();
				generator.invokeConstructor(ASMHelper.TYPE_OBJECT, new Method(ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC));
				generator.returnValue();
				generator.endMethod();
			}
			{
				generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, HANDLER_FUNC_NAME, HANDLER_FUNC_DESC, null, null);
				Tool.iteratorStream(callback.instructions.iterator()).filter(VarInsnNode.class::isInstance).map(VarInsnNode.class::cast).forEach(var -> var.var += 1);
				callback.accept(generator);
				Type returnType = Type.getReturnType(callback.desc);
				if (returnType.getSort() == Type.VOID)
					generator.pushNull();
				else if (ASMHelper.isUnboxType(returnType))
					generator.box(returnType);
				generator.returnValue();
				generator.endMethod();
			}
			cw.visitEnd();
			Class<?> ret = define(uniqueName, cw.toByteArray());
			return $(ret, "new");
		}
		
	}
	
	private static final ASMClassLoader asm_loader = new ASMClassLoader();
	
	public static ASMClassLoader getASMClassLoader() { return asm_loader; }
	
	static {
		AlchemyDebug.start(SETUP_CORE);
		// Initialization index.alchemy.util.ReflectionHelper
		forName("index.alchemy.util.ReflectionHelper");
		// Initialization index.alchemy.util.JFXHelper
		forName("index.alchemy.util.JFXHelper");
		// Initialization index.alchemy.core.debug.JFXDialog
		forName("index.alchemy.core.debug.JFXDialog");
		// Check MeowTweaker runtime Throwable
		AlchemyThrowables.checkThrowables();
		// Should not to implicitly exit(com.sun.javafx.tk.Toolkit) when the last window is closed
		JFXHelper.setImplicitExit(false);
		// Initialization index.alchemy.util.DeobfuscatingRemapper
		Tool.load(DeobfuscatingRemapper.class);
		// Load SrgMap from net.minecraftforge.gradle.GradleStart.srg.srg-mcp or use FMLDeobfuscatingRemapper 
		if (!isRuntimeDeobfuscationEnabled() && System.getProperty(SRG_MCP) != null) {
			$(DeobfuscatingRemapper.class, "INSTANCE<<", new SrgMap(Tool.readSafe(new File(System.getProperty(SRG_MCP)))));
			logger.info("srg-mcp: " + System.getProperty(SRG_MCP));
		}
		// Set class byte array provider
		$(ASMHelper.class, "getClassByteArray<<", (Function<String, byte[]>)
				name -> Tool.getClassByteArray(getLaunchClassLoader(), isRuntimeDeobfuscationEnabled() ?
						DeobfuscatingRemapper.instance().unmapType(name.replace('.', '/')) : name.replace('.', '/')));
		// Load all Alchemy's DLCs
		AlchemyDLCLoader.setup();
		// Load all Alchemy and Alchemy's DLC of the class transformer 
		AlchemyTransformerManager.setup();
		// Register index.alchemy.core.asm.transformer.AlchemyTransformerManager
		registerTransformer(AlchemyTransformerManager.class);
		// Other class transformer should not be behind Alchemy
		// See index.alchemy.core.asm.transformer.TransformerPatch#L113 { node.version = V1_8 }
		// Fixed conflict with enderio
		$(getLaunchClassLoader(), "transformers<<", new ArrayList<IClassTransformer>(2) {
			
			private static final long serialVersionUID = 4954143578755689919L;

			{ add(new TransformerReplace()); }
			
			{ addAll($(getLaunchClassLoader(), "transformers")); }
			
			public boolean add(IClassTransformer e) {
				return add0(-1, e);
			}
			
			public void add(int index, IClassTransformer e) {
				add0(index, e);
			}
			
			protected boolean add0(int index, IClassTransformer e) {
				if (isEmpty() || e.getClass().getName().startsWith(MOD_PACKAGE) ||
						(e instanceof TransformerWrapper && $(e, "parent").getClass().getName().startsWith(MOD_PACKAGE)) ||
						e.getClass().getName().equals("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer"))
					return super.add(e);
				else if (index == -1) {
					for (int i = 0, len = size(); i < len; i++) {
						Object transformer = get(i);
						if (transformer.getClass().getName().startsWith(MOD_PACKAGE) && transformer.getClass() != TransformerReplace.class) {
							super.add(i, e);
							return true;
						}
					}
					super.add(e);
				} else {
					int flag = -1;
					for (int i = 0, len = size(); i < len; i++) {
						Object transformer = get(i);
						if (transformer.getClass().getName().startsWith(MOD_PACKAGE) && transformer.getClass() != TransformerReplace.class) {
							flag = i;
							break;
						}
					}
					if (index <= flag || flag == -1)
						super.add(index, e);
					else
						add(e);
				}
				return true;
			}
			
			public boolean addAll(Collection<? extends IClassTransformer> c) {
				boolean flag[] = { false };
				c.forEach(e -> flag[0] = add(e) || flag[0]);
				return flag[0];
			}
			
			public boolean addAll(int index, Collection<? extends IClassTransformer> c) {
				boolean flag[] = { false };
				List<IClassTransformer> transformers = Lists.newArrayList(c);
				Collections.reverse(transformers);
				transformers.forEach(e -> flag[0] = add0(index, e) || flag[0]);
				return flag[0];
			}
			
		});
		ModContainerFactory.instance().registerContainerType(Type.getType(Alchemy.class), AlchemyModContainer.class);
		forName("net.minecraftforge.fml.common.eventhandler.Event");
		AlchemyDebug.end(SETUP_CORE);
	}
	
	public static void addRuntimeExtLibFromJRE(String name) {
		checkInvokePermissions();
		try {
			URL url = new File(System.getProperty("java.home") + "/lib/ext/" + name + ".jar").toURI().toURL();
			Tool.addURLToClassLoader(getLaunchClassLoader(), url);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}
	
	public static void registerTransformer(Class<? extends IClassTransformer> clazz) {
		checkInvokePermissions();
		getLaunchClassLoader().registerTransformer(clazz.getName());
	}
	
	@SuppressWarnings("unchecked")
	public static void registerTransformer(IClassTransformer transformer) {
		checkInvokePermissions();
		((List<IClassTransformer>) $(getLaunchClassLoader(), "transformers")).add(transformer);
	}
	
	public static void checkInvokePermissions() {
		Tool.checkInvokePermissions(3, AlchemyEngine.class);
	}
	
	public static Set<String> findClassFromURL(URL url) throws Exception {
		Set<String> result = Sets.newLinkedHashSet();
		ClassLoader loader = new URLClassLoader(new URL[]{ url }, null);
		ClassPath path = ClassPath.from(loader);
		for (ClassInfo info : path.getAllClasses())
			if (!info.getName().matches(".*\\$[0-9]+") && !info.getName().contains("$$"))
				result.add(info.getName());
		return result;
	}
	
	private static File alchemyCoreLocation;
	
	@Nullable
	public static File getAlchemyCoreLocation() { return alchemyCoreLocation; }
	
	public static File getMinecraftDir() { return Optional.ofNullable(Launch.minecraftHome).orElseGet(() -> new File(".")); }
	
	@Override
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void injectData(Map<String, Object> data) { alchemyCoreLocation = (File) data.get("coremodLocation"); }

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"index.alchemy.core.asm.transformer.TransformerDeobfuscating",
				"index.alchemy.core.asm.transformer.TransformerGenericEvent"
		};
	}
	
	@Omega
	public static class ModContainer extends DummyModContainer  {
		
		public ModContainer() { this(new ModMetadata()); }
		
		public ModContainer(ModMetadata metadata) {
			super(metadata);
			metadata.modId = CORE_MOD_ID;
			metadata.name = CORE_MOD_NAME;
			metadata.version = CORE_MOD_VERSION;
			metadata.authorList = Arrays.asList(AUTHORLIST);
			metadata.description = "Alchemy mod core, as the pre-loading mod.";
			metadata.credits = "Mojang AB, and the Forge and FML guys. ";
		}
		
		@Override
		public boolean registerBus(EventBus bus, LoadController controller) { return true; }

	}

	@Override
	public String getModContainerClass() {
		return "index.alchemy.core.AlchemyEngine$ModContainer";
	}

	@Override
	public String getSetupClass() {
		return "index.alchemy.core.AlchemySetup";
	}

	@Override
	public String getAccessTransformerClass() {
		// move to AlchemyEngine#<clinit>
		return null/* index.alchemy.core.asm.transformer.AlchemyTransformerManager */;
	}
	
}
