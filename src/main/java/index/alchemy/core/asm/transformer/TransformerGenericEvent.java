package index.alchemy.core.asm.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

@Omega
public class TransformerGenericEvent implements IClassTransformer {
	
	public static final String
			SUBSCRIBE_EVENT_ANNOTATION_DESC = "Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;",
			ITEMSTACK_DESC = "net/minecraft/item/ItemStack",
			ITEMSTACK_GET_ITEM_METHOD_DESC = "()Lnet/minecraft/item/Item;",
			srgName = AlchemyEngine.isRuntimeDeobfuscationEnabled() ? "func_77973_b" :
				DeobfuscatingRemapper.instance().mapMethodName(ITEMSTACK_DESC, "func_77973_b", ITEMSTACK_GET_ITEM_METHOD_DESC);

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.startsWith(MOD_PACKAGE))
			return basicClass;
		try {
			boolean flag = false;
			ClassReader reader = new ClassReader(basicClass);
			ClassNode node = new ClassNode(ASM5);
			reader.accept(node, ClassReader.EXPAND_FRAMES);
			for (MethodNode method : node.methods)
				if (method.visibleAnnotations != null)
					for (AnnotationNode ann : method.visibleAnnotations)
						if (ann.desc.equals(SUBSCRIBE_EVENT_ANNOTATION_DESC) && method.signature != null) {
							String generic = ASMHelper.getGeneric(method.signature);
							if (!generic.isEmpty()) {
								String type = ASMHelper.removeGeneric(ASMHelper.getGenericType(generic)[0]),
											clazz = ASMHelper.getClassName(type);
								if (Type.getArgumentTypes(method.desc)[0].equals(
										Type.getType("Lnet/minecraftforge/event/AttachCapabilitiesEvent;")) &&
										!ASMHelper.isPrimaryClass(clazz)) {
									flag = true;
									String new_generic = "";
									Class<?> super_class = null;
									try {
										super_class = AlchemyEngine.getLaunchClassLoader().loadClass(clazz.replace('/', '.'));
									} catch (ClassNotFoundException e) { AlchemyRuntimeException.onException(e); }
									do 
										if (ASMHelper.isPrimaryClass(super_class.getName())) {
											new_generic = "<L" + super_class.getName().replace('.', '/') + ";>";
											break;
										}
									while ((super_class = super_class.getSuperclass()) != null);
									method.signature = method.signature.replace(generic, new_generic);
									for (LocalVariableNode localVar : method.localVariables)
										if (localVar.signature != null)
											localVar.signature = localVar.signature.replace(generic, new_generic);
									MethodInsnNode getObject = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/event/AttachCapabilitiesEvent",
											"getObject", "()Ljava/lang/Object;", false);
									TypeInsnNode checkCast = new TypeInsnNode(CHECKCAST, ITEMSTACK_DESC);
									MethodInsnNode getItem = new MethodInsnNode(INVOKEVIRTUAL, ITEMSTACK_DESC, srgName,
											"()Lnet/minecraft/item/Item;", false);
									InsnList list = new InsnList();
									LabelNode label = new LabelNode(), getItemLabel0 = new LabelNode();
									list.add(ASMHelper.getIntNode(0));
									list.add(new ASMHelper.DynamicVarInsnNode(ISTORE, 0));
									list.add(new VarInsnNode(ALOAD, (method.access & ACC_STATIC) == 0 ? 1 : 0));
									list.add(getObject);
									list.add(new InsnNode(DUP));
									list.add(new TypeInsnNode(INSTANCEOF, ITEMSTACK_DESC));
									list.add(new JumpInsnNode(IFEQ, getItemLabel0));
									list.add(checkCast);
									list.add(getItem);
									list.add(ASMHelper.getIntNode(1));
									list.add(new ASMHelper.DynamicVarInsnNode(ISTORE, 0));
									list.add(getItemLabel0);
									if (generic.contains("+")) {
										list.add(new TypeInsnNode(INSTANCEOF, clazz));
										list.add(new JumpInsnNode(IFNE, label));
									} else {
										list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass",
												"()Ljava/lang/Class;", false));
										list.add(new LdcInsnNode(Type.getType(ASMHelper.getClassDesc(clazz))));
										list.add(new JumpInsnNode(IF_ACMPEQ, label));
									}
									list.add(new InsnNode(RETURN));
									list.add(label);
									Supplier<InsnList> supplier = () -> {
										InsnList result = new InsnList();
										LabelNode getItemLabel1 =  new LabelNode();
										result.add(new ASMHelper.DynamicVarInsnNode(ILOAD, 0));
										result.add(new JumpInsnNode(IFEQ, getItemLabel1));
										result.add(new TypeInsnNode(CHECKCAST, ITEMSTACK_DESC));
										result.add(new MethodInsnNode(INVOKEVIRTUAL, ITEMSTACK_DESC, srgName,
												"()Lnet/minecraft/item/Item;", false));
										result.add(getItemLabel1);
										return result;
									};
									StreamSupport.stream(Spliterators.spliteratorUnknownSize(method.instructions.iterator(), 0), false)
										.filter(MethodInsnNode.class::isInstance)
										.map(MethodInsnNode.class::cast)
										.filter(methodInsn -> methodInsn.owner.equals(getObject.owner))
										.filter(methodInsn -> methodInsn.name.equals(getObject.name))
										.filter(methodInsn -> methodInsn.desc.equals(getObject.desc))
										.collect(Collectors.toList())
										.stream()
										.forEach(methodInsn -> method.instructions.insert(methodInsn, supplier.get()));
									method.instructions.insert(list);
									int stackSize = Type.getArgumentTypes(method.desc).length;
									if ((method.access & ACC_STATIC) == 0)
										stackSize++;
									ASMHelper.normalizationDynamicVarInsn(method.instructions, stackSize);
								}
								break;
							}
						}
			if (!flag)
				return basicClass;
			ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			Tool.dumpClass(writer.toByteArray(), "d:/" + transformedName + ".bytecode");
			return writer.toByteArray();
		} catch (Exception e) { e.printStackTrace(); throw e; }
	}
	
}
