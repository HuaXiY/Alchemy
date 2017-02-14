package index.alchemy.dlcs.exnails.core;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;

import static org.objectweb.asm.Opcodes.*;

public class TransformerWorld_FixTAN implements IAlchemyClassTransformer {

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName = "func_175727_C";
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled())
			srgName = DeobfuscatingRemapper.instance().mapMethodName(transformedName, srgName,
					"(Lnet/minecraft/util/math/BlockPos;)Z");
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		boolean flag = false;
		for (MethodNode method : node.methods)
			if (method.name.equals(srgName)) {
				AlchemyTransformerManager.transform("<fix>" + name + "|" + transformedName + "#" + srgName + method.desc);
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (flag) {
						if (insn.getOpcode() == ICONST_0)
							iterator.remove();
						break;
					}
					if (insn instanceof MethodInsnNode) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;
						if (methodInsn.owner.equals("toughasnails/season/SeasonASMHelper") && methodInsn.name.equals("isRainingAtInSeason"))
							flag = true;
					}
				}
			}
		if (!flag)
			return basicClass;
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() {
		return "net.minecraft.world.World";
	}

}
