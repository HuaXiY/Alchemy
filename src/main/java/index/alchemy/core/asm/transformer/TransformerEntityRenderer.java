package index.alchemy.core.asm.transformer;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.objectweb.asm.Opcodes.*;

@Omega
@SideOnly(Side.CLIENT)
public class TransformerEntityRenderer implements IAlchemyClassTransformer {

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (true) return basicClass;
		String srgMethodName = "func_175068_a"/* renderWorldPass */, srgFieldName = "field_175078_W"/* debugView */,
				clazzName = ASMHelper.getClassName(transformedName);
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled()) {
			srgMethodName = DeobfuscatingRemapper.INSTANCE.mapMethodName(clazzName, srgMethodName, "(IFJ)V");
			srgFieldName = DeobfuscatingRemapper.INSTANCE.mapFieldName(clazzName, srgFieldName, "Z");
		}
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		insn_node: for (MethodNode method : node.methods)
			if (method.name.equals(srgMethodName)) {
				AlchemyTransformerManager.transform("<remove>" + name + "|" + transformedName + "#" + srgMethodName + method.desc);
				int i = 0;
				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof FieldInsnNode) {
						FieldInsnNode field = (FieldInsnNode) insn;
						if (field.getOpcode() == GETFIELD && 
								field.owner.equals(clazzName) && field.name.equals(srgFieldName) && field.desc.equals("Z")) {
							if (i == 1) {
								AbstractInsnNode first = field.getPrevious();
								LabelNode label = ((JumpInsnNode) field.getNext()).label;
								AbstractInsnNode last = label.getNext();
								insn = insn.getPrevious().getPrevious();
								AbstractInsnNode next = insn;
								while (next != last) {
									insn = next;
									next = next.getNext();
									method.instructions.remove(insn);
								}
								break insn_node;
							} else
								i++;
						}
					}
				}
			}
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() {
		return "net.minecraft.client.renderer.EntityRenderer";
	}

}
