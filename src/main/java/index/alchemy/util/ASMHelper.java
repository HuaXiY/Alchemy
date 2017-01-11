package index.alchemy.util;

import java.io.IOException;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import index.project.version.annotation.Omega;

import static org.objectweb.asm.Opcodes.*;

@Omega
public class ASMHelper {
	
	public static final int getReturnOpcode(Type type) {
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
	
	public static final int getLoadOpcode(Type type) {
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
	
	public static final int getStoreOpcode(Type type) {
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
	
	public static final int getStackFrameLength(Type type) {
		switch (type.getSort()) {
			case Type.LONG:
			case Type.DOUBLE:
				return 2;
			default:
				return 1;
		}
	}
	
	public static final AbstractInsnNode getIntNode(int value) {
		if (value <= 5 && -1 <= value)
			return new InsnNode(ICONST_M1 + value + 1);
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
			return new IntInsnNode(BIPUSH, value);
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
			return new IntInsnNode(SIPUSH, value);
		return new LdcInsnNode(value);
	}
	
	public static final String getClassName(Class<?> clazz) {
		return getClassName(clazz.getName());
	}
	
	public static final String getClassName(String clazz) {
		return clazz.indexOf('L') == 0 && clazz.indexOf(';') == clazz.length() - 1 ?
				clazz.replaceFirst("L", "").replace('.', '/').replace(";", "") : clazz.replace('.', '/').replace(";", "");
	}
	
	public static final String getClassDesc(Class<?> clazz) {
		return getClassDesc(clazz.getName());
	}
	
	public static final String getClassDesc(String clazz) {
		return "L" + getClassName(clazz) + ";";
	}
	
	public static final String getClassSrcName(String name) {
		return getClassName(name).replace('/', '.');
	}
	
	public static final boolean isOverOpcode(int opcode) {
		return opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW;
	}
	
	public static final boolean isPrimaryClass(String name) {
		String temp[] = name.replace('/', '.').split("\\.");
		return temp.length > 1 && temp[temp.length - 1].equalsIgnoreCase(temp[temp.length - 2]);
	}
	
	public static final String getGeneric(String signature) {
		return Tool.get(signature, "(<.*>)");
	}
	
	public static final String[] getGenericType(String generic) {
		Type types[] = Type.getArgumentTypes("(" + removeGeneric(Tool.get(generic, "<(.*)>")).replace("+", "").replace("-", "") + ")V");
		String result[] = new String[types.length];
		for (int i = 0; i < types.length; i++)
			result[i] = types[i].getDescriptor();
		return result;
	}
	
	public static final String removeGeneric(String signature) {
		return signature.replaceAll("(<.*>)", "");
	}
	
	@Nullable
	public static final ClassNode getSuperClassNode(String name) {
		try {
			ClassReader reader = new ClassReader(name);
			ClassNode result = new ClassNode();
			reader.accept(result, 0);
			return getSuperClassNode(result);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	public static final ClassNode getSuperClassNode(ClassNode node) {
		if (node.superName == null || node.superName.isEmpty())
			return null;
		else {
			try {
				ClassReader reader = new ClassReader(node.superName);
				ClassNode result = new ClassNode();
				reader.accept(result, 0);
				return result;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
