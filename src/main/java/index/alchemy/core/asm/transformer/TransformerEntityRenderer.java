package index.alchemy.core.asm.transformer;

import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
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
		String renderWorldPass = "func_175068_a", debugView = "field_175078_W",
				updateCameraAndRender = "func_181560_a", thePlayer = "field_71439_g", getRenderViewEntity = "func_175606_aa",
				clazzName = ASMHelper.getClassName(transformedName), minecraft = "net/minecraft/client/Minecraft";
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled()) {
			renderWorldPass = DeobfuscatingRemapper.instance().mapMethodName(clazzName, renderWorldPass, "(IFJ)V");
			debugView = DeobfuscatingRemapper.instance().mapFieldName(clazzName, debugView, "Z");
			updateCameraAndRender = DeobfuscatingRemapper.instance().mapMethodName(clazzName, updateCameraAndRender, "(FJ)V");
			thePlayer = DeobfuscatingRemapper.instance().mapFieldName(clazzName, thePlayer, "Lnet/minecraft/client/entity/EntityPlayerSP;");
			getRenderViewEntity = DeobfuscatingRemapper.instance().mapMethodName(minecraft, getRenderViewEntity, "()Lnet/minecraft/entity/Entity;");
		}
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods) {
//			if (method.name.equals(renderWorldPass)) {
//				AlchemyTransformerManager.transform("<remove>" + name + "|" + transformedName + "#" + renderWorldPass + method.desc);
//				int i = 0;
//				for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
//					AbstractInsnNode insn = iterator.next();
//					if (insn instanceof FieldInsnNode) {
//						FieldInsnNode field = (FieldInsnNode) insn;
//						if (field.getOpcode() == GETFIELD && 
//								field.owner.equals(clazzName) && field.name.equals(debugView) && field.desc.equals("Z")) {
//							if (i == 1) {
//								AbstractInsnNode first = field.getPrevious();
//								LabelNode label = ((JumpInsnNode) field.getNext()).label;
//								AbstractInsnNode last = label.getNext();
//								insn = insn.getPrevious().getPrevious();
//								AbstractInsnNode next = insn;
//								while (next != last) {
//									insn = next;
//									next = next.getNext();
//									method.instructions.remove(insn);
//								}
//								break insn_node;
//							} else
//								i++;
//						}
//					}
//				}
//			}
			if (method.name.equals(updateCameraAndRender)) {
				AlchemyTransformerManager.transform("<fix>" + name + "|" + transformedName + "#" + updateCameraAndRender + method.desc);
				boolean flag = false;
				for (ListIterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (insn instanceof FieldInsnNode) {
						FieldInsnNode field = (FieldInsnNode) insn;
						if (field.getOpcode() == GETFIELD && 
								field.owner.equals(minecraft) && field.name.equals(thePlayer) &&
								field.desc.equals("Lnet/minecraft/client/entity/EntityPlayerSP;")) {
							iterator.remove();
							iterator.add(new MethodInsnNode(INVOKEVIRTUAL, minecraft,
									"getRenderViewEntity", "()Lnet/minecraft/entity/Entity;", false));
							flag = true;
						}
					}
					if (flag && insn instanceof MethodInsnNode) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;
						methodInsn.owner = "net/minecraft/entity/Entity";
						flag = false;
					}
				}
			}
		}
		node.accept(writer);
		return writer.toByteArray();
	}

	@Override
	public String getTransformerClassName() { return "net.minecraft.client.renderer.EntityRenderer"; }

}
