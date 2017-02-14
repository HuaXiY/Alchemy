package index.alchemy.core.asm.transformer;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static org.objectweb.asm.Opcodes.*;

public enum MeowTweaker implements ITweaker, IClassTransformer {
	
	Sayaka() {
		{
			if (AlchemyEngine.JAVA_VERSION >= '⑨' / 1000)
				try {
					LaunchClassLoader loader = AlchemyEngine.getLaunchClassLoader();
					Field transformersField = loader.getClass().getDeclaredField("transformers");
					transformersField.setAccessible(true);
					List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(loader);
					transformers.add(this);
				} catch(Exception e) { AlchemyRuntimeException.onException(e); }
		}
	};
	
	public static MeowTweaker instance() { return Sayaka; }

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) { /* O.o */ }

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) { /* ٩(｡·ω·｡)﻿و （๑ • ‿ • ๑ ） */ }

	@Override
	public String getLaunchTarget() { return null; }

	@Override
	public String[] getLaunchArguments() { return new String[0]; }

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("index.alchemy.util.ReflectionHelper"))
			try {
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
				long overrideFieldOffset = AlchemyEngine.unsafe().objectFieldOffset(overrideField);
				adapter.loadArg(0);
				adapter.dup();
				adapter.push(overrideFieldOffset);
				adapter.push(1);
				adapter.invokeStatic(Type.getType(AlchemyEngine.unsafe().getClass()), new Method("putBoolean", Type.getMethodDescriptor(
						Type.VOID_TYPE, Type.getType(Object.class), Type.LONG_TYPE, Type.BOOLEAN_TYPE)));
				adapter.returnValue();
				adapter.endMethod();
				node.methods.remove(target);
				node.methods.add(newMethod);
			} catch(Exception e) { AlchemyRuntimeException.onException(e); }
		return basicClass;
	}

}
