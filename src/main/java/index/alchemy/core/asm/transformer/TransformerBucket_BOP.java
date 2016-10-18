package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;

import static org.objectweb.asm.Opcodes.*;

public class TransformerBucket_BOP implements IAlchemyClassTransformer {

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		List<MethodInsnNode> list = new LinkedList<>();
		MethodNode init = null;
		for (MethodNode method : node.methods)
			if (method.name.equals("init")) {
				AlchemyTransformerManager.transform("<change>" + name + "|" + transformedName + "#" + method.name + method.desc);
				init = method;
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof MethodInsnNode) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;
						if (methodInsn.owner.equals("net/minecraftforge/fluids/FluidContainerRegistry") &&
								methodInsn.name.equals("registerFluidContainer") && Type.getArgumentTypes(methodInsn.desc).length < 3) {
							methodInsn.desc = "(Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z";
							list.add(methodInsn);
						}
					}
				}
			}
		if (init != null)
			for (MethodInsnNode insn : list)
				init.instructions.insert(insn.getPrevious(), new FieldInsnNode(GETSTATIC, "net/minecraftforge/fluids/FluidContainerRegistry",
									"EMPTY_BUCKET", "Lnet/minecraft/item/ItemStack;"));
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() {
		return "biomesoplenty.common.init.ModBlocks";
	}

}
