package index.alchemy.core.asm.transformer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyThrowables;
import index.alchemy.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.objectweb.asm.Opcodes.ASM5;

@SuppressWarnings("unchecked")
public enum MeowTweaker implements ITweaker, IClassTransformer {

    Kyouko() {
        @Override
        public boolean shouldInitialization() {
            try {
                Class.forName("javafx.scene.Node", false, AlchemyEngine.getLaunchClassLoader());
                return false;
            } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError ignore) {
                return true;
            }
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
                } catch (Throwable t) {
                    t.printStackTrace();
                    AlchemyThrowables.throwables().add(t);
                }
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
                } catch (Throwable t) { AlchemyThrowables.throwables().add(t); }
        }
    }

    public abstract boolean shouldInitialization();

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
