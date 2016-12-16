package index.alchemy.core.asm.transformer;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;
import index.project.version.annotation.Omega;

import static org.objectweb.asm.Opcodes.*;

@Omega
public class TransformerBootstrap implements IAlchemyClassTransformer {

	// Why not set the timeout ?!
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			String methodName = "<clinit>", fieldName = "BLOCKED_SERVERS";
			ClassReader reader = new ClassReader(basicClass);
			ClassWriter writer = new ClassWriter(0);
			ClassNode node = new ClassNode(ASM5);
			reader.accept(node, 0);
			insn_node: for (MethodNode method : node.methods)
				if (method.name.equals(methodName)) {
					AlchemyTransformerManager.transform("<remove>" + name + "|" + transformedName + "#" + methodName + method.desc);
					method.tryCatchBlocks.clear();
					for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
						AbstractInsnNode insn = iterator.next();
						if (insn instanceof FieldInsnNode) {
							FieldInsnNode field = (FieldInsnNode) insn;
							if (field.getOpcode() == GETSTATIC && field.name.equals(fieldName)) {
								AbstractInsnNode first = field.getPrevious();
								AbstractInsnNode next = insn;
								int i = 0;
								while (!(next instanceof LabelNode && i++ == 2)) {
									insn = next;
									next = next.getNext();
									method.instructions.remove(insn);
								}
								break insn_node;
							}
						}
					}
				}
			node.accept(writer);
			return writer.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return basicClass;
		}
	}

	@Override
	public String getTransformerClassName() {
		return "io.netty.bootstrap.Bootstrap";
	}

}
