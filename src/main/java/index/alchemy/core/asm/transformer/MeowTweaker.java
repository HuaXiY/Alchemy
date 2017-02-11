package index.alchemy.core.asm.transformer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import index.alchemy.core.AlchemyEngine;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static org.objectweb.asm.Opcodes.*;

public enum MeowTweaker implements ITweaker, IClassTransformer {
	
	INSTANCE() {
		{
			if (AlchemyEngine.JAVA_VERSION >= 9)
				try {
					LaunchClassLoader loader = AlchemyEngine.getLaunchClassLoader();
					Field transformersField = loader.getClass().getDeclaredField("transformers");
					transformersField.setAccessible(true);
					List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(loader);
					transformers.add(this);
				} catch(Exception e) { throw new RuntimeException(e); }
		}
	};
	
	
	
	public static MeowTweaker instance() { return INSTANCE; }

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) { /* O.o */ }

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) { /* ٩(｡·ω·｡)﻿و （๑ • ‿ • ๑ ） */ }

	@Override
	public String getLaunchTarget() {
		return null;
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("index.alchemy.util.ReflectionHelper")) {
			ClassNode node = new ClassNode(ASM5);
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(node, 0);
		}
		return basicClass;
	}

}
