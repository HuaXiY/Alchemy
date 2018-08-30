package index.alchemy.core.asm.transformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.project.version.annotation.Alpha;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.util.$.$;

@Alpha
public class TransformerFieldAccess implements IClassTransformer {
	
	protected static final String I_FIELD_ACCESS_DESC = ASMHelper.getClassName("index.alchemy.api.IFieldAccess");
	
	private static int id = -1;
	protected static synchronized int nextId() {
		return ++id;
	}
	
	protected final String owner;
	protected final FieldNode accessField;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		AlchemyTransformerManager.transform("<access>" + name + "|" + transformedName + "#" + accessField.name + " : " +
				accessField.signature + "\n->  " + owner + "#" + accessField.name + " : " + accessField.signature);
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassNode node = ASMHelper.newClassNode(basicClass);
		String signature = ASMHelper.getGenericType(ASMHelper.getGeneric(accessField.signature))[1];
		if (signature.isEmpty())
			signature = "Ljava/lang/Object;";
		String desc = ASMHelper.removeGeneric(signature);
		node.fields.removeIf(f -> f.name.equals(accessField.name));
		node.fields.add(new FieldNode(ACC_PUBLIC, accessField.name, desc, signature, null));
		AlchemyTransformerManager.markClinitCallback(node, () -> updateAccessField(transformedName, accessField, desc), "FieldAccess");
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public void updateAccessField(String clazzName, FieldNode field, String desc) {
		$("L" + owner, field.name + "<<", createAccess(clazzName, field, desc, false));
	}
	
	@Unsafe(Unsafe.ASM_API)
	public static <O, T> IFieldAccess<O, T> createAccess(String clazzName, FieldNode field, String fieldDesc, boolean isStatic) {
		ClassWriter cw = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS);
		ASMHelper.MethodGenerator generator;
		String name = getUniqueName(clazzName, field), nameDesc = ASMHelper.getClassName(name),
				getDesc = Type.getMethodDescriptor(ASMHelper.TYPE_OBJECT, ASMHelper.TYPE_OBJECT),
				setDesc = Type.getMethodDescriptor(Type.VOID_TYPE, ASMHelper.TYPE_OBJECT, ASMHelper.TYPE_OBJECT);
		Type targetType = Type.getObjectType(ASMHelper.getClassName(clazzName)), fieldType = Type.getObjectType(ASMHelper.getClassName(fieldDesc));
		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, nameDesc, null, ASMHelper.OBJECT_NAME, new String[]{ I_FIELD_ACCESS_DESC });
		cw.visitSource("TransformerFieldAccess.java:62", "invoke: " + clazzName + "." + field.name);
		// IFieldAccess::<init>
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC, null, null);
			generator.loadThis();
			generator.invokeConstructor(ASMHelper.TYPE_OBJECT, new Method(ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC));
			generator.returnValue();
			generator.endMethod();
		}
		// IFieldAccess::get
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, "get", getDesc, null, null);
			if (!isStatic) {
				generator.loadArg(0);
				generator.checkCast(targetType);
			}
			if (isStatic)
				generator.getStatic(targetType, field.name, fieldType);
			else
				generator.getField(targetType, field.name, fieldType);
			generator.checkCast(fieldType);
			generator.returnValue();
			generator.endMethod();
		}
		// IFieldAccess::set
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, "set", setDesc, null, null);
			if (!isStatic) {
				generator.loadArg(0);
				generator.checkCast(targetType);
			}
			generator.loadArg(1);
			generator.checkCast(fieldType);
			if (isStatic)
				generator.putStatic(targetType, field.name, fieldType);
			else
				generator.putField(targetType, field.name, fieldType);
			generator.returnValue();
			generator.endMethod();
		}
		cw.visitEnd();
		return $(AlchemyEngine.getASMClassLoader().define(name, cw.toByteArray()), "new");
	}
	
	private static String getUniqueName(String name, FieldNode field) {
		return String.format(
				"%s_%d_%s_%s_%s",
				AlchemyEngine.getASMClassLoader().getClass().getName(),
				nextId(),
				name.replace('.', '_'),
				field.name,
				ASMHelper.getStdName(ASMHelper.getClassName(field.desc))
		);
	}

	public TransformerFieldAccess(String owner, FieldNode accessField) {
		this.owner = owner;
		this.accessField = accessField;
	}

}
