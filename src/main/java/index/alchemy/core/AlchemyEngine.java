package index.alchemy.core;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jooq.lambda.Unchecked;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.asm.transformer.MeowTweaker;
import index.alchemy.core.asm.transformer.SrgMap;
import index.alchemy.core.asm.transformer.TransformerInjectOptifine;
import index.alchemy.core.asm.transformer.TransformerReplace;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.alchemy.util.JFXHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.$;

@Omega
@Name(MOD_ID)
@MCVersion(MC_VERSION)
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions(MOD_TRANSFORMER_PACKAGE)
public class AlchemyEngine implements IFMLLoadingPlugin {
	
	private static final String SETUP_CORE = "setup core", SRG_MCP = "net.minecraftforge.gradle.GradleStart.srg.srg-mcp";
	
	public static final double JAVA_VERSION = Optional.of(System.getProperty("java.specification.version")).map(Double::parseDouble).get();
	
	public static final PrintStream
			sysout = new PrintStream(new FileOutputStream(FileDescriptor.out)),
			syserr = new PrintStream(new FileOutputStream(FileDescriptor.err));
	
	private static final sun.misc.Unsafe unsafe = Unchecked.supplier(AlchemyEngine::getUnsafe).get();
	
	public static final sun.misc.Unsafe unsafe() { return unsafe; }
	
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
		String libArgs = System.getProperty("index.alchemy.runtime.lib.ext");
		Set<String> libs = libArgs != null ? Sets.newHashSet(Splitter.on(';').split(libArgs)) : Sets.newHashSet();
		// Forge hack native libs when startup
		libs.add("jfxrt");
		libs.forEach(AlchemyEngine::addRuntimeExtLibFromJRE);
	}
	
	static { /* Java9  Tweaker */ MeowTweaker.Sayaka(); }
	
	static { /* JavaFX Tweaker */ MeowTweaker.Kyouko(); }
	
	private static boolean runtimeDeobfuscationEnabled = !Boolean.getBoolean("index.alchemy.runtime.deobf.disable");
	
	public static boolean isRuntimeDeobfuscationEnabled() { return runtimeDeobfuscationEnabled; }
	
	static {
		AlchemyDebug.start(SETUP_CORE);
		// Initialization index.alchemy.util.ReflectionHelper
		Tool.forName("index.alchemy.util.ReflectionHelper");
		// Initialization index.alchemy.util.JFXHelper
		Tool.forName("index.alchemy.util.JFXHelper");
		// Initialization index.alchemy.core.debug.JFXDialog
		Tool.forName("index.alchemy.core.debug.JFXDialog");
		// Check MeowTweaker runtime Throwable
		AlchemyThrowables.checkThrowables();
		// Should not to implicitly exit(com.sun.javafx.tk.Toolkit) when the last window is closed
		JFXHelper.setImplicitExit(false);
		// Initialization index.alchemy.util.DeobfuscatingRemapper
		Tool.load(DeobfuscatingRemapper.class);
		// Load SrgMap from net.minecraftforge.gradle.GradleStart.srg.srg-mcp or use FMLDeobfuscatingRemapper 
		if (!isRuntimeDeobfuscationEnabled() && System.getProperty(SRG_MCP) != null)
			$(DeobfuscatingRemapper.class, "INSTANCE<<", new SrgMap(Tool.readSafe(new File(System.getProperty(SRG_MCP)))));
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
		$(getLaunchClassLoader(), "transformers<<", new ArrayList(2) {
			
			{ add(new TransformerReplace()); }
			
			{ addAll($(getLaunchClassLoader(), "transformers")); }
			
			{ removeIf(MeowTweaker.class::isInstance); }
			
			public boolean add(Object e) {
				if (e.getClass().getName().startsWith(MOD_PACKAGE) ||
						(e instanceof TransformerWrapper && $(e, "parent").getClass().getName().startsWith(MOD_PACKAGE)) ||
						e.getClass().getName().equals("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer"))
					return super.add(e);
				else
					for (int i = 0, len = size(); i < len; i++) {
						Object transformer = get(i);
						if (transformer.getClass().getName().startsWith(MOD_PACKAGE) &&
								!(transformer instanceof MeowTweaker) &&
								transformer.getClass() != TransformerInjectOptifine.class) {
							super.add(i, e);
							return true;
						}
					}
				return true;
			}
			
		});
		Tool.forName("net.minecraftforge.fml.common.eventhandler.Event");
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
	
	public static void registerTransformer(IClassTransformer transformer) {
		checkInvokePermissions();
		((List<IClassTransformer>) $(getLaunchClassLoader(), "transformers")).add(transformer);
	}
	
	public static void checkInvokePermissions() {
		Tool.checkInvokePermissions(3, AlchemyEngine.class);
	}
	
	public static class ASMClassLoader extends ClassLoader {
		
		private static final String HANDLER_DESC = Type.getInternalName(Function.class);
		private static final String HANDLER_FUNC_NAME = Tool.searchMethod(Function.class, "apply", Object.class).getName();
		private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(Tool.searchMethod(Function.class, "apply", Object.class));
		
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
			cw.visitSource("AlchemyEngine.java:221", "invoke: " + instType + handleName + handleDesc);
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
				Class<?> ret = define(name, cw.toByteArray());
				if (isStatic)
					result = (Function) ret.newInstance();
				else
					result = (Function) ret.getConstructor(Object.class).newInstance(target);
			} catch(Exception e) { AlchemyRuntimeException.onException(e); }
			return result;
		}
		
	}
	
	private static final ASMClassLoader asm_loader = new ASMClassLoader();
	
	public static ASMClassLoader getASMClassLoader() { return asm_loader; }
	
	private static final MethodHandles.Lookup lookup = $(MethodHandles.Lookup.class, "new", Object.class, -1);
	
	public static MethodHandles.Lookup lookup() { return lookup; }
	
	public static List<String> findClassFromURL(URL url) throws Exception {
		List<String> result = Lists.newLinkedList();
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
	
	public static File getMinecraftDir() { return $(CoreModManager.class, "mcDir"); }
	
	public static LaunchClassLoader getLaunchClassLoader() { return Launch.classLoader; }
	
	public static Side runtimeSide() { return FMLLaunchHandler.side(); }
	
	@Override
	public void injectData(Map<String, Object> data) { alchemyCoreLocation = (File) data.get("coremodLocation"); }

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"index.alchemy.core.asm.transformer.TransformerSideLambda",
				"index.alchemy.core.asm.transformer.TransformerDeobfuscating",
				"index.alchemy.core.asm.transformer.TransformerGenericEvent"
		};
	}

	@Override
	public String getModContainerClass() {
		return "index.alchemy.core.AlchemyModContainer";
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
