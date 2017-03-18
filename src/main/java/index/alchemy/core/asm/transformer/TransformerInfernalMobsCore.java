package index.alchemy.core.asm.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.util.ASMHelper;

import static org.objectweb.asm.Opcodes.*;

public class TransformerInfernalMobsCore implements IAlchemyClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassReader reader = new ClassReader(basicClass);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		node.methods.removeIf(m -> !m.name.equals("<init>"));
		node.methods.stream()
			.map(m -> m.instructions)
			.peek(InsnList::clear)
			.peek(l -> l.add(new VarInsnNode(ALOAD, 0)))
			.peek(l -> l.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)))
			.forEach(l -> l.add(new InsnNode(RETURN)));
		node.interfaces.clear();
		node.superName = "java/lang/Object";
		node.innerClasses.clear();
		ClassWriter writer = ASMHelper.newClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() { return "atomicstryker.infernalmobs.common.InfernalMobsCore"; }

}
