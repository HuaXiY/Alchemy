package index.alchemy.core.asm.transformer;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyCorePlugin;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import static org.objectweb.asm.Opcodes.*;

public class TransformerNetHandlerPlayServer implements IAlchemyClassTransformer {

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName = "func_147347_a";
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			srgName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(transformedName, srgName,
					"(Lnet/minecraft/network/play/client/CPacketPlayer;)V");
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
			if (method.name.equals(srgName)) {
				AlchemyTransformerManager.transform(name + "|" + transformedName + "#" + srgName + method.desc);
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof LdcInsnNode) {
						LdcInsnNode ldc = (LdcInsnNode) insn;
						if (ldc.cst.equals(100.0F) || ldc.cst.equals(300.0F))
							ldc.cst = 900.0F;
					}
				}
			}
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() {
		return "net.minecraft.network.NetHandlerPlayServer";
	}

}
