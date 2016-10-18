package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import index.alchemy.api.annotation.Unsafe;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

public class TransformerProxy implements IClassTransformer {
	
	protected final ClassNode proxy;
	protected String proxyName, clazzName, superName;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		AlchemyTransformerManager.transform(name + "|" + transformedName + "\n->  " + proxy.name.replace('/', '.'));
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		proxyName = proxy.name;
		clazzName = transformedName.replace('.', '/');
		superName = node.superName;
		proxy.methods.forEach(m -> proxyMethod(m, clazzName, superName));
		proxy.methods.forEach(m -> proxyMethod(m, proxyName, clazzName));
		Map<MethodNode, MethodNode> mapping = new LinkedHashMap<>();
		for (MethodNode method : node.methods)
			for (Iterator<MethodNode> iterator = proxy.methods.iterator(); iterator.hasNext();) {
				MethodNode proxyMethod = iterator.next();
				if (method.name.equals(proxyMethod.name) && method.desc.equals(proxyMethod.desc))
					if (!method.name.startsWith("<"))
						mapping.put(method, proxyMethod);
					else {
						ListIterator<AbstractInsnNode> insnListIterator = method.instructions.iterator(method.instructions.size());
						for (AbstractInsnNode insn = insnListIterator.previous(); insnListIterator.hasPrevious();) {
							insn = insnListIterator.previous();
							insnListIterator.remove();
							if (insn instanceof LabelNode)
								break;
						}
						if (method.name.equals("<init>"))
							for (Iterator<AbstractInsnNode> insnIterator = proxyMethod.instructions.iterator(); insnIterator.hasNext();) {
								AbstractInsnNode insn = insnIterator.next();
								insnIterator.remove();
								if (insn instanceof MethodInsnNode)
									break;
							}
						method.instructions.add(proxyMethod.instructions);
						iterator.remove();
					}
			}
		for (Entry<MethodNode, MethodNode> entry : mapping.entrySet()) {
			int index = node.methods.indexOf(entry.getKey());
			node.methods.remove(index);
			node.methods.add(index, entry.getValue());
			proxy.methods.remove(entry.getValue());
		}
		node.methods.addAll(proxy.methods);
		node.fields.addAll(proxy.fields);
		node.interfaces.addAll(proxy.interfaces);
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public void proxyMethod(MethodNode methodNode, String proxyName, String clazzName) {
		methodNode.desc = methodNode.desc.replace(proxyName, clazzName);
		for (Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext();) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof FieldInsnNode) {
				FieldInsnNode field = (FieldInsnNode) insn;
				field.desc = field.desc.replace(proxyName, clazzName);
				field.owner = field.owner.replace(proxyName, clazzName);
			} else if (insn instanceof LdcInsnNode) {
				LdcInsnNode ldc = (LdcInsnNode) insn;
				if (ldc.cst instanceof String)
					ldc.cst = ((String) ldc.cst).replace(proxyName, clazzName);
			} else if (insn instanceof MethodInsnNode) {
				MethodInsnNode method = (MethodInsnNode) insn;
				method.desc = method.desc.replace(proxyName, clazzName);
				method.owner = method.owner.replace(proxyName, clazzName);
			} else if (insn instanceof TypeInsnNode) {
				TypeInsnNode type = (TypeInsnNode) insn;
				type.desc = type.desc.replace(proxyName, clazzName);
			} else if (insn instanceof MultiANewArrayInsnNode) {
				MultiANewArrayInsnNode multiANewArray = (MultiANewArrayInsnNode) insn;
				multiANewArray.desc = multiANewArray.desc.replace(proxyName, clazzName);
			} else if (insn instanceof FrameNode) {
				FrameNode frame = (FrameNode) insn;
				if (frame.local != null)
					frame.local.replaceAll((o -> o instanceof String ? ((String) o).replace(proxyName, clazzName) : o));
				if (frame.stack != null)
					frame.stack.replaceAll((o -> o instanceof String ? ((String) o).replace(proxyName, clazzName) : o));
			}
		}
	}
	
	public TransformerProxy(ClassNode proxy) {
		this.proxy = proxy;
	}

}
