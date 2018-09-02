package index.alchemy.core.asm.transformer;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;

import index.alchemy.api.annotation.Patch;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.V1_8;

@Omega
public class TransformerPatch implements IClassTransformer {

    protected final ClassNode patch;
    protected String patchName, clazzName, superName;

    @Override
    @Unsafe(Unsafe.ASM_API)
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;
        AlchemyTransformerManager.transform("<patch>" + name + "|" + transformedName + "\n->  " + patch.name.replace('/', '.'));
        ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode node = ASMHelper.newClassNode(basicClass), patch = ASMHelper.newClassNode(this.patch);
        for (Iterator<MethodNode> iterator = patch.methods.iterator(); iterator.hasNext(); )
            if (!checkMethodNode(iterator.next(), node))
                iterator.remove();
        for (Iterator<FieldNode> iterator = patch.fields.iterator(); iterator.hasNext(); )
            if (!checkFieldNode(iterator.next(), node))
                iterator.remove();
        patchName = patch.name;
        clazzName = transformedName.replace('.', '/');
        superName = node.superName;
        patch.methods.forEach(m -> patchMethod(m, clazzName, superName, true));
        patch.methods.forEach(m -> patchMethod(m, patchName, clazzName, false));
        Map<MethodNode, MethodNode> mapping = Maps.newLinkedHashMap();
        List<MethodNode> sources = Lists.newLinkedList();
        for (MethodNode method : node.methods)
            for (Iterator<MethodNode> iterator = patch.methods.iterator(); iterator.hasNext(); ) {
                MethodNode patchMethod = iterator.next();
                if (method.name.equals(patchMethod.name) && method.desc.equals(patchMethod.desc))
                    if (!method.name.startsWith("<")) {
                        MethodNode copy = null;
                        for (Iterator<AbstractInsnNode> insnIterator = patchMethod.instructions.iterator(); insnIterator.hasNext(); ) {
                            AbstractInsnNode insn = insnIterator.next();
                            if (insn instanceof MethodInsnNode) {
                                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                                if (ASMHelper.corresponding(patchMethod, node.name, methodInsn)) {
                                    if (copy == null) {
                                        method.accept(copy = new MethodNode(method.access, method.name, method.desc, method.signature,
                                                method.exceptions.toArray(new String[method.exceptions.size()])));
                                        copy.name = getMethodName(node, copy.name);
                                        for (Iterator<AbstractInsnNode> copyIterator = copy.instructions.iterator(); copyIterator.hasNext(); ) {
                                            AbstractInsnNode copyInsn = copyIterator.next();
                                            if (copyInsn instanceof MethodInsnNode) {
                                                MethodInsnNode copyMethodInsn = (MethodInsnNode) copyInsn;
                                                if (ASMHelper.corresponding(method, node.name, copyMethodInsn))
                                                    copyMethodInsn.name = copy.name;
                                            }
                                        }
                                    }
                                    methodInsn.name = copy.name;
                                }
                                if (methodInsn.name.equals(patchMethod.name) && methodInsn.desc.equals(patchMethod.desc) &&
                                        patchMethod.visibleTypeAnnotations != null) {
                                    Type args[] = Type.getArgumentTypes(methodInsn.desc);
                                    for (int i = 0; i < patchMethod.visibleTypeAnnotations.size(); i++) {
                                        TypeAnnotationNode ann = patchMethod.visibleTypeAnnotations.get(i);
                                        if (ann != null && ann.desc.equals(AlchemyTransformerManager.PATCH_GENERIC_ANNOTATION_DESC)) {
                                            Patch.Generic generic = Tool.makeAnnotation(Patch.Generic.class, ann.values);
                                            args[i] = Type.getType(generic.value());
                                            break;
                                        }
                                    }
                                    methodInsn.desc = Type.getMethodDescriptor(Type.getReturnType(methodInsn.desc), args);
                                }
                            }
                        }
                        if (copy != null)
                            sources.add(copy);
                        mapping.put(method, patchMethod);
                    } else {
                        ListIterator<AbstractInsnNode> insnListIterator = method.instructions.iterator(method.instructions.size());
                        for (AbstractInsnNode insn = insnListIterator.previous(); insnListIterator.hasPrevious(); ) {
                            insnListIterator.remove();
                            if (insn instanceof InsnNode)
                                break;
                            insn = insnListIterator.previous();
                        }
                        if (method.name.equals(ASMHelper._INIT_)) {
                            if (!method.desc.equals(patchMethod.desc))
                                continue;
                            MethodInsnNode superCall = ASMHelper.findSuperCall(patchMethod, superName);
                            Objects.requireNonNull(superCall);
                            for (Iterator<AbstractInsnNode> insnIterator = patchMethod.instructions.iterator(); insnIterator.hasNext(); ) {
                                AbstractInsnNode insn = insnIterator.next();
                                insnIterator.remove();
                                if (insn == superCall)
                                    break;
                            }
                            superCall = null;
                            if (patchMethod.visibleAnnotations != null)
                                for (AnnotationNode ann : patchMethod.visibleAnnotations)
                                    if (ann.desc.equals(AlchemyTransformerManager.PATCH_REPLACE_ANNOTATION_DESC)) {
                                        superCall = ASMHelper.findSuperCall(method, superName);
                                        Objects.requireNonNull(superCall);
                                        boolean flag = false;
                                        for (Iterator<AbstractInsnNode> insnIterator = method.instructions.iterator(); insnIterator.hasNext(); ) {
                                            AbstractInsnNode insn = insnIterator.next();
                                            if (flag)
                                                insnIterator.remove();
                                            else if (insn == superCall)
                                                flag = true;
                                        }
                                    }
                        }
                        method.instructions = ASMHelper.NodeCopier.merge(method.instructions, patchMethod.instructions);
                        iterator.remove();
                    }
            }
        for (MethodNode method : patch.methods)
            if (method.name.equals(ASMHelper._INIT_))
                if (method.visibleAnnotations != null)
                    for (AnnotationNode ann : method.visibleAnnotations)
                        if (ann.desc.equals(AlchemyTransformerManager.PATCH_SUPER_ANNOTATION_DESC)) {
                            MethodInsnNode superCall = ASMHelper.findSuperCall(method, clazzName);
                            superCall.owner = replace(superCall.owner, clazzName, superName);
                        }
        for (Entry<MethodNode, MethodNode> entry : mapping.entrySet()) {
            int index = node.methods.indexOf(entry.getKey());
            node.methods.remove(index);
            node.methods.add(index, entry.getValue());
            patch.methods.remove(entry.getValue());
        }
        node.methods.addAll(sources);
        node.methods.addAll(patch.methods);
        patch.fields.forEach(newField -> node.fields.removeIf(oldField -> Objects.equals(newField.name, oldField.name)));
        node.fields.addAll(patch.fields);
        node.interfaces.addAll(patch.interfaces);
        node.interfaces.remove(node.name);
        ASMHelper.requestMinVersion(node, V1_8).accept(writer);
        return writer.toByteArray();
    }

    public static String getMethodName(ClassNode node, String name) {
        String newName = "$runtime_source$_" + name;
        for (MethodNode method : node.methods)
            if (method.name.equals(newName))
                newName = getMethodName(node, newName);
        return newName;
    }

    public static boolean checkFieldNode(FieldNode field, ClassNode node) {
        if (field.visibleAnnotations == null)
            return true;
        for (AnnotationNode ann : field.visibleAnnotations)
            if (ann.desc.equals(AlchemyTransformerManager.PATCH_EXCEPTION_ANNOTATION_DESC))
                return false;
            else if (ann.desc.equals(AlchemyTransformerManager.PATCH_SPARE_ANNOTATION_DESC))
                for (FieldNode nowField : node.fields)
                    if (field.name.equals(nowField.name) && field.desc.equals(nowField.desc))
                        return false;
        return true;
    }

    public static boolean checkMethodNode(MethodNode method, ClassNode node) {
        if (method.visibleAnnotations == null)
            return true;
        for (AnnotationNode ann : method.visibleAnnotations)
            if (ann.desc.equals(AlchemyTransformerManager.PATCH_EXCEPTION_ANNOTATION_DESC))
                return false;
            else if (ann.desc.equals(AlchemyTransformerManager.PATCH_SPARE_ANNOTATION_DESC))
                for (MethodNode nowMethod : node.methods)
                    if (method.name.equals(nowMethod.name) && method.desc.equals(nowMethod.desc))
                        return false;
        return true;
    }

    public static void patchMethod(MethodNode methodNode, String patchName, String clazzName, boolean isSuper) {
        methodNode.desc = methodNode.desc.replace(patchName, clazzName);
        for (Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
            AbstractInsnNode insn = iterator.next();
            if (insn instanceof TypeInsnNode) {
                TypeInsnNode type = (TypeInsnNode) insn;
                type.desc = replace(type.desc, patchName, clazzName);
            } else if (insn instanceof FieldInsnNode) {
                if (!isSuper) {
                    FieldInsnNode field = (FieldInsnNode) insn;
                    field.owner = replace(field.owner, patchName, clazzName);
                }
            } else if (insn instanceof MethodInsnNode) {
                MethodInsnNode method = (MethodInsnNode) insn;
                boolean flag = !isSuper || method.getOpcode() == INVOKESPECIAL;
                if (flag)
                    method.owner = replace(method.owner, patchName, clazzName);
            } else if (insn instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) insn;
                String patchDesc = ASMHelper.getClassDesc(patchName), clazzDesc = ASMHelper.getClassDesc(clazzName);
                dynamic.desc = dynamic.desc.replace(patchDesc, clazzDesc);
                dynamic.bsm = new Handle(dynamic.bsm.getTag(), replace(dynamic.bsm.getOwner(), patchName, clazzName),
                        dynamic.bsm.getName(), dynamic.bsm.getDesc().replace(patchDesc, clazzDesc), dynamic.bsm.isInterface());
                for (int i = 0; i < dynamic.bsmArgs.length; i++)
                    if (dynamic.bsmArgs[i] instanceof Handle) {
                        Handle handle = (Handle) dynamic.bsmArgs[i];
                        dynamic.bsmArgs[i] = new Handle(handle.getTag(), replace(handle.getOwner(), patchName, clazzName),
                                handle.getName(), handle.getDesc().replace(patchDesc, clazzDesc), ((Handle) dynamic.bsmArgs[i]).isInterface());
                    }
            } else if (insn instanceof FrameNode) {
                FrameNode frame = (FrameNode) insn;
                if (frame.local != null)
                    frame.local.replaceAll((o -> o instanceof String ? replace((String) o, patchName, clazzName) : o));
                if (frame.stack != null)
                    frame.stack.replaceAll((o -> o instanceof String ? replace((String) o, patchName, clazzName) : o));
            }
        }
    }

    protected static String replace(String src, String patch, String clazz) {
        return src.equals(patch) ? clazz : src;
    }

    public TransformerPatch(ClassNode patch) {
        patch.accept(this.patch = new ClassNode());
    }

    @Unsafe(Unsafe.ASM_API)
    public static byte[] patchClassTransformerCallback(@Nullable Module module, ClassLoader loader, String name, @Nullable Class<?> target,
                                                       ProtectionDomain domain, byte buffer[]) {
        if (target != null) {
            Patch patch = target.getAnnotation(Patch.class);
            if (patch != null)
                try {
                    List<IClassTransformer> transformers = AlchemyTransformerManager.transformers_mapping.get(patch.value());
                    TransformerPatch oldTransformer = transformers.stream()
                            .filter(TransformerPatch.class::isInstance)
                            .map(TransformerPatch.class::cast)
                            .filter(transformer -> ASMHelper.getClassSrcName(transformer.patch.name).equals(patch.value()))
                            .findAny()
                            .orElse(null);
                    if (oldTransformer != null)
                        transformers.remove(oldTransformer);
                    AlchemyTransformerManager.loadPatch(ASMHelper.newClassNode(buffer));
                    AlchemyEngine.redefineClass(AlchemyEngine.getLaunchClassLoader().findClass(patch.value()));
                } catch (Throwable e) { e.printStackTrace(); }
        }
        return buffer;
    }

    static {
        Instrumentation instrumentation = AlchemyEngine.instrumentation();
        if (instrumentation != null)
            instrumentation.addTransformer(AlchemyEngine.IClassFileTransformer.of(TransformerPatch::patchClassTransformerCallback));
    }

}
