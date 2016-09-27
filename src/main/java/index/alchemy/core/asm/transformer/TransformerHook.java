package index.alchemy.core.asm.transformer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerHook implements IClassTransformer {
	
	protected final MethodNode hookMethod;
	protected final String owner, srgName;
	protected final boolean isStatic;
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		AlchemyTransformerManager.transform(name + "|" + transformedName + "#" + srgName);
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
			if (method.name.equals(srgName) && checkMethodNode(method)) {
				Type returnType = Type.getReturnType(method.desc);
				int returnOpcode = ASMHelper.getReturnOpcode(returnType);
				InsnList list = new InsnList();
				for (int i = 0, len = Type.getArgumentTypes(hookMethod.desc).length; i < len; i++)
					list.add(new VarInsnNode(ALOAD, i));
				list.add(new MethodInsnNode(INVOKESTATIC, AlchemyTransformerManager.ALCHEMY_HOOKS_DESC, hookMethod.name,
						hookMethod.desc, false));
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
						list.add(new MethodInsnNode(INVOKEVIRTUAL, returnType.getSort() == Type.BOOLEAN ?
								ASMHelper.getClassName(Boolean.class) : ASMHelper.getClassName(Number.class),
								returnType.getClassName() + "Value", Type.getMethodDescriptor(returnType), false));
						break;
					case ARETURN:
					case RETURN:
					default:
				}
				list.add(new InsnNode(returnOpcode));
				list.add(label);
				method.instructions.insert(list);
			}
		node.accept(writer);
		return writer.toByteArray();
	}
	
	protected boolean checkMethodNode(MethodNode method) {
		List<Type> hookMethodTypes = Arrays.asList(Type.getArgumentTypes(hookMethod.desc)),
				srcMethodTypes = Arrays.asList(Type.getArgumentTypes(method.desc));
		if (!isStatic && (method.access & ACC_STATIC) != 0) {
			srcMethodTypes = new LinkedList<Type>(srcMethodTypes);
			srcMethodTypes.add(0, Type.getType(ASMHelper.getClassDesc(owner)));
		}
		return hookMethodTypes.equals(srcMethodTypes);
	}

	@Unsafe(Unsafe.REFLECT_API)
	public TransformerHook(MethodNode hookMethod, String owner, String srgName, boolean isStatic) {
		this.hookMethod = hookMethod;
		this.owner = ASMHelper.getClassName(owner);
		String desc, result = null;
		Type args[] = Type.getArgumentTypes(hookMethod.desc);
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
			AlchemyRuntimeException.onException(e);
		}
		this.srgName = Tool.isNullOr(result, srgName);
		this.isStatic = isStatic;
	}

}
