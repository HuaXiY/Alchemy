package index.alchemy.core.asm.transformer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyCorePlugin;
import index.alchemy.core.AlchemyHooks;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerHook implements IClassTransformer {
	
	protected final MethodNode hookMethod;
	protected final String owner, srgName;
	protected final boolean isStatic;
	protected final Hook.Type type;
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
			if (method.name.equals(srgName) && checkMethodNode(method)) {
				AlchemyTransformerManager.transform(name + "|" + transformedName + "#" + srgName + method.desc + "\n->  " +
						AlchemyHooks.class.getName() + "#" + hookMethod.name + hookMethod.desc);
				Type args[] = Type.getArgumentTypes(method.desc), returnType = Type.getReturnType(method.desc);
				int returnOpcode = ASMHelper.getReturnOpcode(returnType);
				insn_node: for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (type == Hook.Type.HEAD || ASMHelper.isOverOpcode(insn.getOpcode())) {
						InsnList list = new InsnList();
						if (!isStatic)
							list.add(new VarInsnNode(ALOAD, 0));
						for (int i = 0, offset = isStatic ? 0 : 1, len = args.length; i < len; i++)
							list.add(new VarInsnNode(ASMHelper.getLoadOpcode(args[i]), i + offset));
						list.add(new MethodInsnNode(INVOKESTATIC, AlchemyTransformerManager.ALCHEMY_HOOKS_DESC, hookMethod.name,
								hookMethod.desc, false));
						if (Type.getReturnType(hookMethod.desc).equals(Type.getType(Hook.Result.class))) {
							list.add(new FieldInsnNode(GETFIELD, AlchemyTransformerManager.HOOK_RESULT_DESC, "result", Type.getDescriptor(Object.class)));
							if (returnOpcode != RETURN)
								list.add(new InsnNode(DUP));
							list.add(new FieldInsnNode(GETSTATIC, ASMHelper.getClassName(Tool.class), "VOID", Type.getDescriptor(Void.class)));
							LabelNode label = new LabelNode(new Label());
							list.add(new JumpInsnNode(IF_ACMPEQ, label));
							switch (returnOpcode) {
								case IRETURN:
								case LRETURN:
								case FRETURN:
								case DRETURN:
									String desc = returnType.getSort() == Type.BOOLEAN ? ASMHelper.getClassName(Boolean.class) :
											returnType.getSort() == Type.CHAR ? ASMHelper.getClassName(Character.class) :
											ASMHelper.getClassName(Number.class);
									list.add(new TypeInsnNode(CHECKCAST, desc));
									list.add(new MethodInsnNode(INVOKEVIRTUAL, desc, returnType.getClassName() + "Value",
											Type.getMethodDescriptor(returnType), false));
									break;
								case ARETURN:
									list.add(new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
									break;
								case RETURN:
								default:
							}
							list.add(new InsnNode(returnOpcode));
							list.add(label);
						}
						switch (type) {
							case TAIL:
								if (insn.getPrevious() != null)
									method.instructions.insert(insn.getPrevious(), list);
								else
									method.instructions.insert(list);
								break;
							case HEAD:
								method.instructions.insert(list);
								break insn_node;
						}
					}
				}
				break;
			}
		node.accept(writer);
		return writer.toByteArray();
	}
	
	protected boolean checkMethodNode(MethodNode method) {
		if (isStatic == ((method.access & ACC_STATIC) == 0))
			return false;
		List<Type> hookMethodTypes = Arrays.asList(Type.getArgumentTypes(hookMethod.desc)),
				srcMethodTypes = Arrays.asList(Type.getArgumentTypes(method.desc));
		if (!isStatic) {
			srcMethodTypes = new LinkedList<Type>(srcMethodTypes);
			srcMethodTypes.add(0, Type.getType(ASMHelper.getClassDesc(owner)));
		}
		return hookMethodTypes.equals(srcMethodTypes);
	}

	@Unsafe(Unsafe.REFLECT_API)
	public TransformerHook(MethodNode hookMethod, String owner, String srgName, boolean isStatic, Hook.Type type) {
		this.hookMethod = hookMethod;
		this.owner = ASMHelper.getClassName(owner);
		String desc, result = null;
		Type args[] = Type.getArgumentTypes(hookMethod.desc);
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled()) {
			StringBuilder builder = new StringBuilder("(");
			for (int i = isStatic ? 0 : 1; i < args.length; i++)
				builder.append(args[i].getDescriptor());
			desc = srgName + builder.append(")").toString();
			try {
				for (Entry<String, String> entry : Tool.<Map<String, String>>$(FMLDeobfuscatingRemapper.INSTANCE, "getMethodMap", this.owner).entrySet())
					if (entry.getKey().startsWith(desc)) {
						result = entry.getValue();
						break;
					}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		this.srgName = Tool.isNullOr(result, srgName);
		this.isStatic = isStatic;
		this.type = type;
	}

}
