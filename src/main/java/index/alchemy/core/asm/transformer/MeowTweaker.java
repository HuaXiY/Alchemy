package index.alchemy.core.asm.transformer;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyThrowables;
import index.alchemy.util.ASMHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static org.objectweb.asm.Opcodes.*;

public enum MeowTweaker implements ITweaker, IClassTransformer {
	
	Sayaka() {
		
		@Override
		public boolean shouldInitialization() {
			return AlchemyEngine.JAVA_VERSION >= '⑨' / 1000;
		}
		
		@Override
		public byte[] transform(String name, String transformedName, byte[] basicClass) {
			if (transformedName.equals("index.alchemy.util.ReflectionHelper"))
				try {
					logger.info("Transform: <meow tweaker>" + name + "|" + transformedName);
					ClassNode node = new ClassNode(ASM5);
					ClassReader reader = new ClassReader(basicClass);
					reader.accept(node, 0);
					MethodNode target = null;
					for (MethodNode method : node.methods)
						if (method.name.equals("setAccessible") &&
								method.desc.equals("(Ljava/lang/reflect/AccessibleObject;)Ljava/lang/reflect/AccessibleObject;"))
							target = method;
					if (target == null)
						throw new RuntimeException(new NullPointerException("target"));
					MethodNode newMethod = new MethodNode(target.access, target.name, target.desc, target.signature,
							target.exceptions.toArray(new String[target.exceptions.size()]));
					GeneratorAdapter adapter = new GeneratorAdapter(newMethod, target.access, target.name, target.desc);
					Field overrideField = AccessibleObject.class.getDeclaredField("override");
					long overrideFieldOffset = getUnsafe().objectFieldOffset(overrideField);
					adapter.loadArg(0);
					adapter.dup();
					adapter.invokeStatic(Type.getType(MeowTweaker.class), new Method("getUnsafe", "()Lsun/misc/Unsafe;"));
					adapter.swap();
					adapter.push(overrideFieldOffset);
					adapter.push(true);
					adapter.invokeVirtual(Type.getType(getUnsafe().getClass()), new Method("putBoolean", Type.getMethodDescriptor(
							Type.VOID_TYPE, Type.getType(Object.class), Type.LONG_TYPE, Type.BOOLEAN_TYPE)));
					adapter.returnValue();
					adapter.endMethod();
					node.methods.remove(target);
					node.methods.add(newMethod);
					ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
					node.accept(writer);
					return writer.toByteArray();
				} catch(Throwable t) { AlchemyThrowables.throwables().add(t); }
			return basicClass;
		}
		
	},
	Kyouko() {
		
		@Override
		public boolean shouldInitialization() {
			try {
				Class.forName("javafx.scene.Node");
				return false;
			} catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError ignore) { return true; }
		}
		
		@Override
		public byte[] transform(String name, String transformedName, byte[] basicClass) {
			if (transformedName.equals("index.alchemy.util.JFXHelper") || transformedName.equals("index.alchemy.core.debug.JFXDialog"))
				try {
					logger.info("Transform: <meow tweaker>" + name + "|" + transformedName);
					ClassNode node = new ClassNode(ASM5);
					ClassReader reader = new ClassReader(basicClass);
					reader.accept(node, 0);
					for (MethodNode method : node.methods)
						ASMHelper.clearMethod(method);
					ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
					node.accept(writer);
					return writer.toByteArray();
				} catch(Throwable t) { t.printStackTrace(); AlchemyThrowables.throwables().add(t); }
			return basicClass;
		}
		
	};
	
	{
		try {
			Class.forName("index.alchemy.core.run.GradleStartAlchemy");
		} catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError ignore) {
			if (shouldInitialization())
				try {
					LaunchClassLoader loader = AlchemyEngine.getLaunchClassLoader();
					Field transformersField = loader.getClass().getDeclaredField("transformers");
					transformersField.setAccessible(true);
					List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(loader);
					transformers.add(0, this);
					setUnsafe(AlchemyEngine.unsafe());
				} catch(Throwable t) { AlchemyThrowables.throwables().add(t); }
		}
	}
	
	public abstract boolean shouldInitialization();
	
	public static MeowTweaker Sayaka() { return Sayaka; }
	
	public static MeowTweaker Kyouko() { return Kyouko; }
	
	public static final Logger logger = LogManager.getLogger(MeowTweaker.class.getSimpleName());
	
	private static sun.misc.Unsafe unsafe;
	
	public static void setUnsafe(sun.misc.Unsafe unsafe) { MeowTweaker.unsafe = unsafe; }
	
	public static sun.misc.Unsafe getUnsafe() { return unsafe; }
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) { /* O.o */ }

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) { /* ٩(｡·ω·｡)﻿و （๑ • ‿ • ๑ ） */ }

	@Override
	public String getLaunchTarget() { return null; }

	@Override
	public String[] getLaunchArguments() { return new String[0]; }

}
