package index.alchemy.core.asm.transformer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.*;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.*;
import index.alchemy.util.cache.ICache;
import index.alchemy.util.cache.StdCache;
import index.project.version.annotation.Omega;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static index.alchemy.core.AlchemyConstants.MOD_PACKAGE;
import static index.alchemy.util.$.$;
import static org.objectweb.asm.Opcodes.*;

@Omega
public class AlchemyTransformerManager implements IClassTransformer {

    public static final String
            I_ALCHEMY_CLASS_TRANSFORMER_DESC = "index/alchemy/api/IAlchemyClassTransformer",
            HOOK_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Hook$Provider;",
            HOOK_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Hook;",
            PROXY_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Proxy$Provider;",
            PROXY_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Proxy;",
            PATCH_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch;",
            PATCH_EXCEPTION_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Exception;",
            PATCH_SPARE_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Spare;",
            PATCH_GENERIC_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Generic;",
            PATCH_REPLACE_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Replace;",
            PATCH_SUPER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Patch$Super;",
            FIELD_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Field$Provider;",
            FIELD_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Field;",
            FIELD_ACCESS_DESC = "Lindex/alchemy/api/IFieldAccess;",
            REMOTE_PROVIDER_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Remote$Provider;",
            REMOTE_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Remote;",
            SIDE_ONLY_ANNOTATION_DESC = "Lnet/minecraftforge/fml/relauncher/SideOnly;",
            CALLBACK_FLAG_DESC = "index/alchemy/core/asm/transformer/AlchemyTransformerManager$CALLBACK_FLAG";

    public static final Logger logger = LogManager.getLogger(AlchemyTransformerManager.class.getSimpleName());

    public static final Set<IClassTransformer> transformers = Sets.newHashSet();
    public static final ICache<String, List<IClassTransformer>> transformers_mapping =
            new StdCache<String, List<IClassTransformer>>().setOnMissGet(Lists::newLinkedList);

    public static final ICache<String, ICache<Runnable, String>> callback_mapping =
            new StdCache<String, ICache<Runnable, String>>().setOnMissGet(StdCache::new);

    @Deprecated
    public interface CALLBACK_FLAG {}

    public static void markClinitCallback(ClassNode node, Runnable runnable, String debugInfo) {
        callback_mapping.get(node.name).add(runnable, debugInfo);
        for (String i : node.interfaces)
            if (i.equals(CALLBACK_FLAG_DESC))
                return;
        node.interfaces.add(CALLBACK_FLAG_DESC);
        MethodNode clinit = null;
        for (MethodNode method : node.methods)
            if (method.name.equals("<clinit>")) {
                clinit = method;
                break;
            }
        boolean flag = clinit == null;
        if (flag)
            node.methods.add(clinit = new MethodNode(0, "<clinit>", "()V", null, null));
        InsnList list = new InsnList();
        list.add(new LdcInsnNode(node.name));
        list.add(new MethodInsnNode(INVOKESTATIC, "index/alchemy/core/asm/transformer/AlchemyTransformerManager",
                "callback", "(Ljava/lang/String;)V", false));
        if (flag)
            list.add(new InsnNode(RETURN));
        clinit.instructions.insert(list);
    }

    public static void callback(@Nonnull String name) {
        ICache<Runnable, String> callback = callback_mapping.del(name);
        if (callback == null)
            return;
        logger.info("<clinit> callback: " + name + " - " + Joiner.on(", ").join(callback.getCacheMap().values()));
        if (callback != null)
            callback.getCacheMap().forEach((runnable, debugInfo) -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    logger.error("Throwable in callback " + name + " : " + debugInfo, t);
                }
            });
    }

    static { ReflectionHelper.setClassLoader(ClassWriter.class, AlchemyEngine.getLaunchClassLoader()); }

    public static void setup() {
        AlchemyEngine.checkInvokePermissions();
        logger.info("Setup: " + AlchemyTransformerManager.class.getName());
        try {
            loadAllProvider();
        } catch (Exception e) { AlchemyRuntimeException.onException(new RuntimeException(e)); }
    }

    public static final boolean debug_print = Boolean.getBoolean("index.alchemy.core.asm.classloader.debug");

    @Unsafe(Unsafe.ASM_API)
    public static void runtimeDebugTargetClass(Class<?> provider, Class<?> target) throws Throwable {
        runtimeDebugTargetClass(AlchemyEngine.getLaunchClassLoader().getClassBytes(provider.getName()), target);
    }

    @Unsafe(Unsafe.ASM_API)
    public static void runtimeDebugTargetClass(byte provider[], Class<?> target) throws Throwable {
        loadTransforms(ASMHelper.newClassNode(provider), $(provider, "new"));
        AlchemyEngine.redefineClass(target);
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadAllProvider() throws Exception {
        for (ClassInfo info : ClassPath.from(AlchemyEngine.makeDomainClassLoader()).getAllClasses())
            if (info.getName().startsWith(MOD_PACKAGE)) {
                ClassNode node = ASMHelper.newClassNode(IOUtils.toByteArray(info.url().openStream()));
                if (checkSideOnly(node))
                    loadTransforms(node, info);
            }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadTransforms(ClassNode node, ClassInfo info) {
        loadPatch(node);
        loadField(node);
        loadRemote(node);
        loadProxy(node);
        loadHook(node);
        loadTransform(node, () -> $(info.load(), "new"));
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadRemote(ClassNode node) {
        if (node.visibleAnnotations != null)
            for (AnnotationNode nann : node.visibleAnnotations)
                if (nann.desc.equals(REMOTE_PROVIDER_ANNOTATION_DESC)) {
                    for (MethodNode methodNode : node.methods)
                        if (checkSideOnly(methodNode) && methodNode.visibleAnnotations != null)
                            for (AnnotationNode ann : methodNode.visibleAnnotations)
                                if (ann.desc.equals(REMOTE_ANNOTATION_DESC)) {
                                    Remote remote = Tool.makeAnnotation(Remote.class, ann.values, "always", true, "sync", true);
                                    transformers_mapping.get(ASMHelper.getClassSrcName(node.name))
                                            .add(new TransformerRemote(methodNode, remote.value(), remote.always(), remote.sync(), node.name));
                                }
                    break;
                }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadPatch(ClassNode node) {
        if (node.visibleAnnotations != null)
            for (AnnotationNode ann : node.visibleAnnotations)
                if (ann.desc.equals(PATCH_ANNOTATION_DESC)) {
                    Patch patch = Tool.makeAnnotation(Patch.class, ann.values);
                    transformers_mapping.get(patch.value()).add(new TransformerPatch(node));
                    break;
                }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadHook(ClassNode node) {
        if (node.visibleAnnotations != null)
            for (AnnotationNode nann : node.visibleAnnotations)
                if (nann.desc.equals(HOOK_PROVIDER_ANNOTATION_DESC)) {
                    for (MethodNode methodNode : node.methods)
                        if (checkSideOnly(methodNode) && methodNode.visibleAnnotations != null)
                            for (AnnotationNode ann : methodNode.visibleAnnotations)
                                if (ann.desc.equals(HOOK_ANNOTATION_DESC)) {
                                    Hook hook = Tool.makeAnnotation(Hook.class, ann.values,
                                            "isStatic", false, "type", Hook.Type.HEAD, "disable", "");
                                    if (hook.disable().isEmpty() || !Boolean.getBoolean(hook.disable())) {
                                        String args[] = hook.value().split("#");
                                        if (args.length == 2)
                                            transformers_mapping.get(args[0]).add(new TransformerHook(methodNode, node.name,
                                                    args[1], hook.isStatic(), (node.access & ACC_INTERFACE) != 0, hook.type()));
                                        else
                                            AlchemyRuntimeException.onException(new RuntimeException("@Hook method -> split(\"#\") != 2"));
                                    }
                                }
                    break;
                }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadProxy(ClassNode node) {
        if (node.visibleAnnotations != null)
            for (AnnotationNode nann : node.visibleAnnotations)
                if (nann.desc.equals(PROXY_PROVIDER_ANNOTATION_DESC)) {
                    String name = ASMHelper.getClassSrcName(node.name);
                    for (MethodNode methodNode : node.methods)
                        if (checkSideOnly(methodNode) && methodNode.visibleAnnotations != null)
                            for (AnnotationNode ann : methodNode.visibleAnnotations)
                                if (ann.desc.equals(PROXY_ANNOTATION_DESC)) {
                                    Proxy proxy = Tool.makeAnnotation(Proxy.class, ann.values, "useHandle", false, "itf", false);
                                    String args[] = proxy.target().split("#");
                                    if (args.length == 2)
                                        transformers_mapping.get(name).add(new TransformerProxy(
                                                methodNode, proxy.opcode(), proxy.useHandle(), proxy.itf(), args[0], args[1]));
                                    else
                                        AlchemyRuntimeException.onException(new RuntimeException("@Proxy method -> split(\"#\") != 2"));
                                }
                    break;
                }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadField(ClassNode node) {
        if (node.visibleAnnotations != null)
            for (AnnotationNode nann : node.visibleAnnotations)
                if (nann.desc.equals(FIELD_PROVIDER_ANNOTATION_DESC)) {
                    for (FieldNode fieldNode : node.fields)
                        if (checkSideOnly(fieldNode) && fieldNode.desc.equals(FIELD_ACCESS_DESC)) {
                            String generics[] = ASMHelper.getGenericType(ASMHelper.getGeneric(fieldNode.signature));
                            if (generics.length > 1)
                                transformers_mapping.get(ASMHelper.getClassSrcName(generics[0]))
                                        .add(new TransformerFieldAccess(ASMHelper.getClassSrcName(node.name), fieldNode));
                        }
                    break;
                }
    }

    @Unsafe(Unsafe.ASM_API)
    public static void loadTransform(ClassNode node, Supplier<IAlchemyClassTransformer> supplier) {
        if (checkSideOnly(node) && checkTransformer(node))
            try {
                IAlchemyClassTransformer transformer = supplier.get();
                if (!transformer.disable())
                    if (transformer.getTransformerClassName() == null)
                        transformers.add(transformer);
                    else
                        transformers_mapping.get(transformer.getTransformerClassName()).add(transformer);
            } catch (Exception e) { AlchemyRuntimeException.onException(e); }
    }

    @Override
    @Unsafe(Unsafe.ASM_API)
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (debug_print)
            logger.info("Loading: " + transformedName);
        for (IClassTransformer transformer : transformers)
            basicClass = transform(name, transformedName, basicClass, transformer);
        if (transformers_mapping.has(transformedName))
            for (IClassTransformer transformer : transformers_mapping.get(transformedName))
                basicClass = transform(name, transformedName, basicClass, transformer);
        return basicClass;
    }

    public byte[] transform(String name, String transformedName, byte[] basicClass, IClassTransformer transformer) {
        try {
            basicClass = transformer.transform(name, transformedName, basicClass);
        } catch (Throwable t) {
            logger.error("Throwable in transformer " + name + "|" + transformedName + " : " + transformer.getClass().getName(), t);
        }
        return basicClass;
    }

    public static boolean checkTransformer(ClassNode node) {
        for (String i : node.interfaces)
            if (i.equals(I_ALCHEMY_CLASS_TRANSFORMER_DESC))
                return true;
        return false;
    }

    public static boolean checkSideOnly(ClassNode node) {
        if (node.visibleAnnotations == null)
            return true;
        for (AnnotationNode annotation : node.visibleAnnotations)
            if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
                return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyEngine.runtimeSide();
        return true;
    }

    public static boolean checkSideOnly(MethodNode node) {
        if (node.visibleAnnotations == null)
            return true;
        for (AnnotationNode annotation : node.visibleAnnotations)
            if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
                return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyEngine.runtimeSide();
        return true;
    }

    public static boolean checkSideOnly(FieldNode node) {
        if (node.visibleAnnotations == null)
            return true;
        for (AnnotationNode annotation : node.visibleAnnotations)
            if (annotation.desc.equals(SIDE_ONLY_ANNOTATION_DESC))
                return Tool.makeAnnotation(SideOnly.class, annotation.values).value() == AlchemyEngine.runtimeSide();
        return true;
    }

    protected static final List<Class<?>> loadedClass = Arrays.asList(AlchemyEngine.instrumentation().getAllLoadedClasses());
    protected static Set<String> classLoaderExceptions = $(AlchemyEngine.getLaunchClassLoader(), "classLoaderExceptions"),
            transformerExceptions = $(AlchemyEngine.getLaunchClassLoader(), "transformerExceptions");

    public static boolean shouldRedefine(Class<?> clazz) {
        return clazz != null && (loadedClass.contains(clazz) || inPackage(clazz.getName()));
    }

    protected static boolean inPackage(String name) {
        for (String pkg : classLoaderExceptions)
            if (name.startsWith(pkg))
                return true;
        for (String pkg : transformerExceptions)
            if (name.startsWith(pkg))
                return true;
        return false;
    }

    public static void loadAllTransformClass() {
        logger.info("Load all the classes that should be transformed.");
        transformers_mapping.getCacheMap()
                .keySet()
                .stream()
                .map($::forName)
                .filter(AlchemyTransformerManager::shouldRedefine)
                .forEach(FunctionHelper.onThrowableConsumer(AlchemyEngine::redefineClass, FunctionHelper::rethrowVoid));
    }

    public static void transform(String name) { logger.info("Transform: " + name); }

}
