package index.alchemy.core.asm.transformer;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.project.version.annotation.Alpha;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.util.Tool.$;

@Alpha
public class TransformerFieldAccess implements IClassTransformer {
	
	public static final String I_FIELD_ACCESS_DESC = Type.getInternalName(IFieldAccess.class);
	
	private static int id = -1;
	public static synchronized int nextId() {
		return ++id;
	}
	
	protected final String owner;
	protected final FieldNode accessField;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		AlchemyTransformerManager.transform("<access>" + name + "|" + transformedName + "#" + accessField.name + " : " +
				accessField.signature + "\n->  " + owner + "#" + accessField.name + " : " + accessField.signature);
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		String signature = ASMHelper.getGenericType(ASMHelper.getGeneric(accessField.signature))[1];
		if (signature.isEmpty())
			signature = "Ljava/lang/Object;";
		String desc = ASMHelper.removeGeneric(signature);
		node.fields.removeIf(f -> f.name.equals(accessField.name));
		node.fields.add(new FieldNode(ACC_PUBLIC, accessField.name, desc, signature, null));
		AlchemyTransformerManager.markClinitCallback(node, () -> updateAccessField(transformedName, accessField, desc));
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public void updateAccessField(String clazzName, FieldNode field, String desc) {
		$("L" + owner, field.name + "<<", createAccess(clazzName, field, desc, false));
	}
	
	@Nullable
	@Unsafe(Unsafe.ASM_API)
	public static IFieldAccess createAccess(String clazzName, FieldNode field, String fieldDesc, boolean isStatic) {
		IFieldAccess result = null;
		
		ClassWriter cw = ASMHelper.newClassWriter(0);
		MethodVisitor mv;

		String name = getUniqueName(clazzName, field), nameDesc = ASMHelper.getClassName(name),
				desc = ASMHelper.getClassName(clazzName), type = ASMHelper.getClassName(fieldDesc);

		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, nameDesc, null, "java/lang/Object", new String[]{ I_FIELD_ACCESS_DESC });
		cw.visitSource("TransformerFieldAccess.java:106", "invoke: " + clazzName + "." + field.name);
		{
			mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			if (!isStatic) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, desc);
			}
			mv.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, desc, field.name, fieldDesc);
			mv.visitTypeInsn(CHECKCAST, type);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			if (!isStatic) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, desc);
			}
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, type);
			mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, desc, field.name, fieldDesc);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		cw.visitEnd();
		
		try {
			Class<?> ret =  AlchemyEngine.getASMClassLoader().define(name, cw.toByteArray());
			result = (IFieldAccess) ret.newInstance();
		} catch(Exception e) { AlchemyRuntimeException.onException(e); }
		return result;
	}
	
	private static String getUniqueName(String name, FieldNode field) {
		return String.format(
				"%s_%d_%s_%s_%s",
				AlchemyEngine.getASMClassLoader().getClass().getName(),
				nextId(),
				name.replace('.', '_'),
				field.name,
				ASMHelper.getClassName(field.desc).replace('/', '_')
		);
	}

	public TransformerFieldAccess(String owner, FieldNode accessField) {
		this.owner = owner;
		this.accessField = accessField;
	}

}
