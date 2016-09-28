package index.alchemy.util;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

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
		return clazz.replace('.', '/');
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
	
}
