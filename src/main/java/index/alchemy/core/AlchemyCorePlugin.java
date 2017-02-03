package index.alchemy.core;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.debug.AlchemyDebug;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
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
public class AlchemyCorePlugin implements IFMLLoadingPlugin {
	
	private static final String SETUP_CORE = "setup core";
	
	static {
		AlchemyDebug.start(SETUP_CORE);
		Set<String> libs = Sets.newHashSet(Splitter.on(';').split(Tool.isEmptyOr(
				System.getProperty("index.alchemy.runtime.lib.ext"), "")));
		libs.add("jfxrt");
		libs.forEach(AlchemyCorePlugin::addRuntimeExtLibFromJRE);
		AlchemyDLCLoader.setup();
		AlchemyTransformerManager.setup();
		registerTransformer(AlchemyTransformerManager.class);
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
	
	public static void checkInvokePermissions() {
		Tool.checkInvokePermissions(3, AlchemyCorePlugin.class);
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
			cw.visitSource("AlchemyCorePlugin.java:105", "invoke: " + instType + handleName + handleDesc);
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
	
	public static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
	
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
	
	private static boolean runtimeDeobfuscationEnabled = !Boolean.getBoolean("index.alchemy.runtime.deobf.disable");
	
	public static boolean isRuntimeDeobfuscationEnabled() { return runtimeDeobfuscationEnabled; }
	
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
		return "index.alchemy.core.asm.transformer.AlchemyTransformerManager";
	}

}
