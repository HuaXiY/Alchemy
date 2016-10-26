package index.alchemy.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nullable;

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
	
	public static final boolean isOverOpcode(int opcode) {
		return opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW;
	}
	
	public static final boolean isPrimaryClass(String name) {
		System.out.println(name);
		System.out.println(name.replace('/', '.'));
		String temp[] = name.replace('/', '.').split("\\.");
		System.out.println(Arrays.toString(temp));
		System.out.println(temp[temp.length - 1]);
		System.out.println(temp[temp.length - 2]);
		return temp.length > 1 && temp[temp.length - 1].equalsIgnoreCase(temp[temp.length - 2]);
	}
	
	@Nullable
	public static final ClassNode getSuperClassNode(String name) {
		System.out.println("getSuperClassNode1: " + name);
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
		System.out.println("getSuperClassNode2: " + node.name);
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
