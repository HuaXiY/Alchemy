package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import index.alchemy.api.annotation.Unsafe;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

@Omega
public class TransformerPatch implements IClassTransformer {
	
	protected final ClassNode patch;
	protected String patchName, clazzName, superName;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		AlchemyTransformerManager.transform("<patch>" + name + "|" + transformedName + "\n->  " + patch.name.replace('/', '.'));
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (Iterator<MethodNode> iterator = patch.methods.iterator(); iterator.hasNext();)
			if (!checkMethodNode(iterator.next()))
				iterator.remove();
		for (Iterator<FieldNode> iterator = patch.fields.iterator(); iterator.hasNext();)
			if (!checkFieldNode(iterator.next()))
				iterator.remove();
		patchName = patch.name;
		clazzName = transformedName.replace('.', '/');
		superName = node.superName;
		patch.methods.forEach(m -> patchMethod(m, clazzName, superName));
		patch.methods.forEach(m -> patchMethod(m, patchName, clazzName));
		Map<MethodNode, MethodNode> mapping = new LinkedHashMap<>();
		for (MethodNode method : node.methods)
			for (Iterator<MethodNode> iterator = patch.methods.iterator(); iterator.hasNext();) {
				MethodNode patchMethod = iterator.next();
				if (method.name.equals(patchMethod.name) && method.desc.equals(patchMethod.desc))
					if (!method.name.startsWith("<"))
						mapping.put(method, patchMethod);
					else {
						ListIterator<AbstractInsnNode> insnListIterator = method.instructions.iterator(method.instructions.size());
						for (AbstractInsnNode insn = insnListIterator.previous(); insnListIterator.hasPrevious();) {
							insn = insnListIterator.previous();
							insnListIterator.remove();
							if (insn instanceof LabelNode)
								break;
						}
						if (method.name.equals("<init>"))
							for (Iterator<AbstractInsnNode> insnIterator = patchMethod.instructions.iterator(); insnIterator.hasNext();) {
								AbstractInsnNode insn = insnIterator.next();
								insnIterator.remove();
								if (insn instanceof MethodInsnNode)
									break;
							}
						method.instructions.add(patchMethod.instructions);
						iterator.remove();
					}
			}
		for (Entry<MethodNode, MethodNode> entry : mapping.entrySet()) {
			int index = node.methods.indexOf(entry.getKey());
			node.methods.remove(index);
			node.methods.add(index, entry.getValue());
			patch.methods.remove(entry.getValue());
		}
		node.methods.addAll(patch.methods);
		node.fields.addAll(patch.fields);
		node.interfaces.addAll(patch.interfaces);
		node.version = V1_8;
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public static boolean checkFieldNode(FieldNode field) {
		if (field.visibleAnnotations == null)
			return true;
		for (AnnotationNode ann : field.visibleAnnotations)
			if (ann.desc.equals(AlchemyTransformerManager.PATCH_EXCEPTION_ANNOTATION_DESC))
				return false;
		return true;
	}
	
	public static boolean checkMethodNode(MethodNode method) {
		if (method.visibleAnnotations == null)
			return true;
		for (AnnotationNode ann : method.visibleAnnotations)
			if (ann.desc.equals(AlchemyTransformerManager.PATCH_EXCEPTION_ANNOTATION_DESC))
				return false;
		return true;
	}
	
	public void patchMethod(MethodNode methodNode, String patchName, String clazzName) {
		methodNode.desc = methodNode.desc.replace(patchName, clazzName);
		for (Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext();) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof FieldInsnNode) {
				FieldInsnNode field = (FieldInsnNode) insn;
				field.desc = field.desc.replace(patchName, clazzName);
				field.owner = field.owner.replace(patchName, clazzName);
			} else if (insn instanceof LdcInsnNode) {
				LdcInsnNode ldc = (LdcInsnNode) insn;
				if (ldc.cst instanceof String)
					ldc.cst = ((String) ldc.cst).replace(patchName, clazzName);
			} else if (insn instanceof MethodInsnNode) {
				MethodInsnNode method = (MethodInsnNode) insn;
				method.desc = method.desc.replace(patchName, clazzName);
				method.owner = method.owner.replace(patchName, clazzName);
			} else if (insn instanceof TypeInsnNode) {
				TypeInsnNode type = (TypeInsnNode) insn;
				type.desc = type.desc.replace(patchName, clazzName);
			} else if (insn instanceof MultiANewArrayInsnNode) {
				MultiANewArrayInsnNode multiANewArray = (MultiANewArrayInsnNode) insn;
				multiANewArray.desc = multiANewArray.desc.replace(patchName, clazzName);
			} else if (insn instanceof InvokeDynamicInsnNode) {
				InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) insn;
				for (int i = 0; i < dynamic.bsmArgs.length; i++)
					if (dynamic.bsmArgs[i] instanceof Handle) {
						Handle handle = (Handle) dynamic.bsmArgs[i];
						dynamic.bsmArgs[i] = new Handle(handle.getTag(), handle.getOwner().replace(patchName, clazzName),
								handle.getName(), handle.getDesc());
					}
			} else if (insn instanceof FrameNode) {
				FrameNode frame = (FrameNode) insn;
				if (frame.local != null)
					frame.local.replaceAll((o -> o instanceof String ? ((String) o).replace(patchName, clazzName) : o));
				if (frame.stack != null)
					frame.stack.replaceAll((o -> o instanceof String ? ((String) o).replace(patchName, clazzName) : o));
			}
		}
	}
	
	public TransformerPatch(ClassNode patch) {
		patch.accept(this.patch = new ClassNode());
	}

}
