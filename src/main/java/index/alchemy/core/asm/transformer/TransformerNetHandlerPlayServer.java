package index.alchemy.core.asm.transformer;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.AlchemyBaubles;
import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.project.version.annotation.Beta;

import static org.objectweb.asm.Opcodes.*;

@Beta
public class TransformerNetHandlerPlayServer implements IAlchemyClassTransformer {

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName1 = "func_147347_a"/* processPlayer */, srgName2 = "func_147344_a"/* processCreativeInventoryAction */;
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled()) {
			srgName1 = DeobfuscatingRemapper.instance().mapMethodName(transformedName, srgName1,
					"(Lnet/minecraft/network/play/client/CPacketPlayer;)V");
			srgName2 = DeobfuscatingRemapper.instance().mapMethodName(transformedName, srgName2,
					"(Lnet/minecraft/network/play/client/CPacketCreativeInventoryAction;)V");
		}
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
			if (method.name.equals(srgName1)) {
				AlchemyTransformerManager.transform("<ldc>" + name + "|" + transformedName + "#" + srgName1 + method.desc);
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof LdcInsnNode) {
						LdcInsnNode ldc = (LdcInsnNode) insn;
						if (ldc.cst.equals(100.0F) || ldc.cst.equals(300.0F))
							ldc.cst = 900.0F;
					}
				}
			} else if (method.name.equals(srgName2)) {
				AlchemyTransformerManager.transform("<ldc>" + name + "|" + transformedName + "#" + srgName2 + method.desc);
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof IntInsnNode) {
						IntInsnNode var = (IntInsnNode) insn;
						if (var.getOpcode() == BIPUSH && var.operand == 45)
							var.operand += AlchemyBaubles.asm_offset;
					}
				}
			}
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() { return "net.minecraft.network.NetHandlerPlayServer"; }

}
