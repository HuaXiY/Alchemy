package index.alchemy.util;

import java.util.ListIterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import index.alchemy.api.annotation.Unsafe;
import index.project.version.annotation.Alpha;
import index.project.version.annotation.Omega;

import static org.objectweb.asm.Opcodes.*;

@Omega
public interface ASMHelper {
	
	static class DynamicVarInsnNode extends VarInsnNode {

		public DynamicVarInsnNode(int opcode, int var) {
			super(opcode, var);
		}

	}
	
	static void normalizationDynamicVarInsn(InsnList list, int stackSize) {
		int max = StreamSupport.stream(Spliterators.spliteratorUnknownSize(list.iterator(), 0), false)
			.filter(VarInsnNode.class::isInstance)
			.filter(((Predicate<AbstractInsnNode>) DynamicVarInsnNode.class::isInstance).negate())
			.map(VarInsnNode.class::cast)
			.map(var -> var.var)
			.max(Integer::compareTo)
			.orElse(0);
		int index = Math.max(max, stackSize);
		StreamSupport.stream(Spliterators.spliteratorUnknownSize(list.iterator(), 0), false)
			.filter(DynamicVarInsnNode.class::isInstance)
			.map(DynamicVarInsnNode.class::cast)
			.forEach(var -> list.set(var, new VarInsnNode(var.getOpcode(), var.var + index)));
	}
	
	static void addLabelNode(InsnList list, LabelNode label) {
		list.add(label);
		list.add(getDefaultFrameNode());
	}
	
	static FrameNode getDefaultFrameNode() {
		return new FrameNode(F_SAME, 0, null, 0, null);
	}
	
	static int getReturnOpcode(Type type) {
		switch (type.getSort()) {
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.BOOLEAN:
				return IRETURN;
			case Type.LONG:
				return LRETURN;
			case Type.FLOAT:
				return FRETURN;
			case Type.DOUBLE:
				return DRETURN;
			case Type.OBJECT:
			case Type.ARRAY:
				return ARETURN;
			case Type.VOID:
			default:
				return RETURN;
		}
	}
	
	static int getLoadOpcode(Type type) {
		switch (type.getSort()) {
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.BOOLEAN:
				return ILOAD;
			case Type.LONG:
				return LLOAD;
			case Type.FLOAT:
				return FLOAD;
			case Type.DOUBLE:
				return DLOAD;
			default:
				return ALOAD;
		}
	}
	
	static int getStoreOpcode(Type type) {
		switch (type.getSort()) {
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.BOOLEAN:
				return ISTORE;
			case Type.LONG:
				return LSTORE;
			case Type.FLOAT:
				return FSTORE;
			case Type.DOUBLE:
				return DSTORE;
			default:
				return ASTORE;
		}
	}
	
	static int getStackFrameLength(Type type) {
		switch (type.getSort()) {
			case Type.LONG:
			case Type.DOUBLE:
				return 2;
			default:
				return 1;
		}
	}
	
	static AbstractInsnNode getIntNode(int value) {
		if (value <= 5 && -1 <= value)
			return new InsnNode(ICONST_M1 + value + 1);
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
			return new IntInsnNode(BIPUSH, value);
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
			return new IntInsnNode(SIPUSH, value);
		return new LdcInsnNode(value);
	}
	
	static String getClassName(Class<?> clazz) {
		return getClassName(clazz.getName());
	}
	
	static String getClassName(String clazz) {
		return clazz.indexOf('L') == 0 && clazz.indexOf(';') == clazz.length() - 1 ?
				clazz.replaceFirst("L", "").replace('.', '/').replace(";", "") : clazz.replace('.', '/').replace(";", "");
	}
	
	static String getClassDesc(Class<?> clazz) {
		return getClassDesc(clazz.getName());
	}
	
	static String getClassDesc(String clazz) {
		return "L" + getClassName(clazz) + ";";
	}
	
	static String getClassSrcName(String name) {
		return getClassName(name).replace('/', '.');
	}
	
	static boolean isOverOpcode(int opcode) {
		return opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW;
	}
	
	static boolean isPrimaryClass(String name) {
		name = name.replace('/', '.');
		if (name.equals("net.minecraft.item.ItemStack"))
			return true;
		String temp[] = name.split("\\.");
		return temp.length > 1 && temp[temp.length - 1].equalsIgnoreCase(temp[temp.length - 2]);
	}
	
 	static String getGeneric(String signature) {
		return Tool.get(signature, "(<.*>)");
	}
	
	static String[] getGenericType(String generic) {
		Type types[] = Type.getArgumentTypes("(" + removeGeneric(Tool.get(generic, "<(.*)>")).replace("+", "").replace("-", "") + ")V");
		String result[] = new String[types.length];
		for (int i = 0; i < types.length; i++)
			result[i] = types[i].getDescriptor();
		return result;
	}
	
	static String removeGeneric(String signature) {
		return signature.replaceAll("(<.*>)", "");
	}
	
	Function<String, byte[]> getClassByteArray = c -> null;
	
	@Nullable
	static ClassNode getClassNode(String name) {
		if (Strings.isNullOrEmpty(name))
			return null;
		try {
			byte bytecode[] = getClassByteArray.apply(name);
			if (bytecode == null)
				bytecode = IOUtils.toByteArray(ASMHelper.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class"));
			ClassReader reader = new ClassReader(bytecode);
			ClassNode result = new ClassNode();
			reader.accept(result, 0);
			return result;
		} catch (Exception e) { return null; }
	}
	
	@Nullable
	static ClassNode getSuperClassNode(String name) {
		return getSuperClassNode(getClassNode(name));
	}
	
	@Nullable
	static ClassNode getSuperClassNode(ClassNode node) {
		if (node == null || node.superName == null || node.superName.isEmpty())
			return null;
		else
			return getClassNode(node.superName);
	}
	
	static boolean corresponding(FieldNode field, String owner, FieldInsnNode fieldInsn) {
		return Objects.equal(field.name, fieldInsn.name)
				&& Objects.equal(field.desc, fieldInsn.desc)
				&& Objects.equal(owner, fieldInsn.owner);
	}
	
	static boolean corresponding(MethodNode method, String owner, MethodInsnNode methodInsn) {
		return Objects.equal(method.name, methodInsn.name)
				&& Objects.equal(method.desc, methodInsn.desc)
				&& Objects.equal(owner, methodInsn.owner);
	}
	
	static AbstractInsnNode getDefaultLdcNode(Type type) {
		switch (type.getSort()) {
			case Type.INT:
				return getIntNode(0);
			case Type.BOOLEAN:
				return new LdcInsnNode(false);
			case Type.BYTE:
				return new LdcInsnNode((byte) 0);
			case Type.SHORT:
				return new LdcInsnNode((short) 0);
			case Type.LONG:
				return new LdcInsnNode(0L);
			case Type.FLOAT:
				return new LdcInsnNode(0F);
			case Type.DOUBLE:
				return new LdcInsnNode(0D);
			case Type.CHAR:
				return new LdcInsnNode((char) 0);
			default:
				return new InsnNode(ACONST_NULL);
		}
	}
	
	static void clearMethod(MethodNode method) {
		Type returnType = Type.getReturnType(method.desc);
		method.instructions.clear();
		if (returnType.getSort() != Type.VOID)
			method.instructions.add(getDefaultLdcNode(returnType));
		method.instructions.add(new InsnNode(getReturnOpcode(returnType)));
	}
	
	static void removeInvoke(ListIterator<AbstractInsnNode> iterator) {
		int offset = getStackFrameOffset(iterator.previous());
		iterator.remove();
		while (offset != 0) {
			if (!iterator.hasPrevious())
				throw new RuntimeException("Unable to get the previous node.");
			AbstractInsnNode prev = iterator.previous();
			offset += getStackFrameOffset(prev);
			iterator.remove();
		}
	}
	
	@Alpha
	@Unsafe(Unsafe.ASM_API)
	static int getStackFrameOffset(AbstractInsnNode insn) {
		int opcode = insn.getOpcode();
		if (insn instanceof InsnNode) {
			if (opcode == NOP)
				return 0;
			if (opcode < ILOAD)
				return 1;
			if (opcode < ISTORE)
				return 0;
			if (opcode < POP)
				return -2;
			// Bugs may occur: POP2
			if (opcode < DUP)
				return -1;
			// Bugs may occur: DUP2, DUP2_X1， DUP2_X2
			if (opcode < SWAP)
				return 1;
			if (opcode == SWAP)
				return 0;
			if (opcode < IINC)
				return -1;
			if (opcode < LCMP)
				return 0;
			if (opcode < IFEQ)
				return -1;
		} else if (insn instanceof MethodInsnNode) {
			MethodInsnNode method = (MethodInsnNode) insn;
			return -Type.getArgumentTypes(method.desc).length + (opcode == INVOKESTATIC ? 0 : -1) +
					(Type.getReturnType(method.desc) == Type.VOID_TYPE ? 0 : 1);
		} else if (insn instanceof FieldInsnNode)
			switch (opcode) {
				case GETSTATIC:
					return 1;
				case PUTSTATIC:
					return -1;
				case GETFIELD:
					return 0;
				case PUTFIELD:
					return -2;
			}
		else if (insn instanceof VarInsnNode)
			return opcode > SALOAD ? -1 : 1;
		else if (insn instanceof LdcInsnNode)
			return 1;
		else if (insn instanceof IntInsnNode)
			return 1;
		else if (insn instanceof TypeInsnNode)
			if (opcode == NEW)
				return 1;
		return 0;
	}
	
	static ClassWriter newClassWriter(int flags) {
		return new ClassWriter(flags) {
			
			@Override
			protected String getCommonSuperClass(String a, String b) {
				ClassNode aNode = getClassNode(a), bNode = getClassNode(b);
				Tool.checkNull(aNode, bNode);
				if (isInstance(aNode, bNode))
					return a;
				if (isInstance(bNode, aNode))
					return b;
				if (isInterface(aNode) || isInterface(bNode))
					return "java/lang/Object";
				do
					aNode = getSuperClassNode(aNode);
				while (!isInstance(aNode, bNode));
				return aNode.name;
			}
			
		};
	}
	
	static boolean isSubclass(ClassNode supers, ClassNode clazz) {
		do
			if (supers.name.equals(clazz.name))
				return true;
		while ((clazz = getSuperClassNode(clazz)) != null);
		return false;
	}
	
	static boolean isInstance(ClassNode supers, ClassNode clazz) {
		for (String i : clazz.interfaces)
			if (isInstance(supers, getClassNode(i)))
				return true;
		return isSubclass(supers, clazz);
	}
	
	static boolean isInterface(ClassNode node) {
		return (node.access & ACC_INTERFACE) != 0;
	}
	
}
