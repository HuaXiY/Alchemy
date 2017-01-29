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
import index.alchemy.core.AlchemyCorePlugin;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

@Omega
public class TransformerGenericEvent implements IClassTransformer {
	
	public static final String SUBSCRIBE_EVENT_ANNOTATION_DESC = "Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;";

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.startsWith(MOD_PACKAGE))
			return basicClass;
		boolean flag = false;
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
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
									super_class = AlchemyCorePlugin.getLaunchClassLoader().loadClass(clazz.replace('/', '.'));
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
								InsnList list = new InsnList();
								LabelNode label = new LabelNode();
								list.add(new VarInsnNode(ALOAD, (method.access & ACC_STATIC) == 0 ? 1 : 0));
								list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/event/AttachCapabilitiesEvent",
										"getObject", "()Ljava/lang/Object;", false));
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
								method.instructions.insert(list);
							}
							break;
						}
					}
		if (!flag)
			return basicClass;
		node.accept(writer);
		return writer.toByteArray();
	}

}
