package index.alchemy.util;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterators;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.project.version.annotation.Alpha;
import index.project.version.annotation.Omega;

import static org.objectweb.asm.Opcodes.*;

@Omega
public interface ASMHelper {
	
	int NOP = 0; // visitInsn
    int ACONST_NULL = 1; // -
    int ICONST_M1 = 2; // -
    int ICONST_0 = 3; // -
    int ICONST_1 = 4; // -
    int ICONST_2 = 5; // -
    int ICONST_3 = 6; // -
    int ICONST_4 = 7; // -
    int ICONST_5 = 8; // -
    int LCONST_0 = 9; // -
    int LCONST_1 = 10; // -
    int FCONST_0 = 11; // -
    int FCONST_1 = 12; // -
    int FCONST_2 = 13; // -
    int DCONST_0 = 14; // -
    int DCONST_1 = 15; // -
    int BIPUSH = 16; // visitIntInsn
    int SIPUSH = 17; // -
    int LDC = 18; // visitLdcInsn
     int LDC_W = 19; // -
     int LDC2_W = 20; // -
    int ILOAD = 21; // visitVarInsn
    int LLOAD = 22; // -
    int FLOAD = 23; // -
    int DLOAD = 24; // -
    int ALOAD = 25; // -
     int ILOAD_0 = 26; // -
     int ILOAD_1 = 27; // -
     int ILOAD_2 = 28; // -
     int ILOAD_3 = 29; // -
     int LLOAD_0 = 30; // -
     int LLOAD_1 = 31; // -
     int LLOAD_2 = 32; // -
     int LLOAD_3 = 33; // -
     int FLOAD_0 = 34; // -
     int FLOAD_1 = 35; // -
     int FLOAD_2 = 36; // -
     int FLOAD_3 = 37; // -
     int DLOAD_0 = 38; // -
     int DLOAD_1 = 39; // -
     int DLOAD_2 = 40; // -
     int DLOAD_3 = 41; // -
     int ALOAD_0 = 42; // -
     int ALOAD_1 = 43; // -
     int ALOAD_2 = 44; // -
     int ALOAD_3 = 45; // -
    int IALOAD = 46; // visitInsn
    int LALOAD = 47; // -
    int FALOAD = 48; // -
    int DALOAD = 49; // -
    int AALOAD = 50; // -
    int BALOAD = 51; // -
    int CALOAD = 52; // -
    int SALOAD = 53; // -
    int ISTORE = 54; // visitVarInsn
    int LSTORE = 55; // -
    int FSTORE = 56; // -
    int DSTORE = 57; // -
    int ASTORE = 58; // -
     int ISTORE_0 = 59; // -
     int ISTORE_1 = 60; // -
     int ISTORE_2 = 61; // -
     int ISTORE_3 = 62; // -
     int LSTORE_0 = 63; // -
     int LSTORE_1 = 64; // -
     int LSTORE_2 = 65; // -
     int LSTORE_3 = 66; // -
     int FSTORE_0 = 67; // -
     int FSTORE_1 = 68; // -
     int FSTORE_2 = 69; // -
     int FSTORE_3 = 70; // -
     int DSTORE_0 = 71; // -
     int DSTORE_1 = 72; // -
     int DSTORE_2 = 73; // -
     int DSTORE_3 = 74; // -
     int ASTORE_0 = 75; // -
     int ASTORE_1 = 76; // -
     int ASTORE_2 = 77; // -
     int ASTORE_3 = 78; // -
    int IASTORE = 79; // visitInsn
    int LASTORE = 80; // -
    int FASTORE = 81; // -
    int DASTORE = 82; // -
    int AASTORE = 83; // -
    int BASTORE = 84; // -
    int CASTORE = 85; // -
    int SASTORE = 86; // -
    int POP = 87; // -
    int POP2 = 88; // -
    int DUP = 89; // -
    int DUP_X1 = 90; // -
    int DUP_X2 = 91; // -
    int DUP2 = 92; // -
    int DUP2_X1 = 93; // -
    int DUP2_X2 = 94; // -
    int SWAP = 95; // -
    int IADD = 96; // -
    int LADD = 97; // -
    int FADD = 98; // -
    int DADD = 99; // -
    int ISUB = 100; // -
    int LSUB = 101; // -
    int FSUB = 102; // -
    int DSUB = 103; // -
    int IMUL = 104; // -
    int LMUL = 105; // -
    int FMUL = 106; // -
    int DMUL = 107; // -
    int IDIV = 108; // -
    int LDIV = 109; // -
    int FDIV = 110; // -
    int DDIV = 111; // -
    int IREM = 112; // -
    int LREM = 113; // -
    int FREM = 114; // -
    int DREM = 115; // -
    int INEG = 116; // -
    int LNEG = 117; // -
    int FNEG = 118; // -
    int DNEG = 119; // -
    int ISHL = 120; // -
    int LSHL = 121; // -
    int ISHR = 122; // -
    int LSHR = 123; // -
    int IUSHR = 124; // -
    int LUSHR = 125; // -
    int IAND = 126; // -
    int LAND = 127; // -
    int IOR = 128; // -
    int LOR = 129; // -
    int IXOR = 130; // -
    int LXOR = 131; // -
    int IINC = 132; // visitIincInsn
    int I2L = 133; // visitInsn
    int I2F = 134; // -
    int I2D = 135; // -
    int L2I = 136; // -
    int L2F = 137; // -
    int L2D = 138; // -
    int F2I = 139; // -
    int F2L = 140; // -
    int F2D = 141; // -
    int D2I = 142; // -
    int D2L = 143; // -
    int D2F = 144; // -
    int I2B = 145; // -
    int I2C = 146; // -
    int I2S = 147; // -
    int LCMP = 148; // -
    int FCMPL = 149; // -
    int FCMPG = 150; // -
    int DCMPL = 151; // -
    int DCMPG = 152; // -
    int IFEQ = 153; // visitJumpInsn
    int IFNE = 154; // -
    int IFLT = 155; // -
    int IFGE = 156; // -
    int IFGT = 157; // -
    int IFLE = 158; // -
    int IF_ICMPEQ = 159; // -
    int IF_ICMPNE = 160; // -
    int IF_ICMPLT = 161; // -
    int IF_ICMPGE = 162; // -
    int IF_ICMPGT = 163; // -
    int IF_ICMPLE = 164; // -
    int IF_ACMPEQ = 165; // -
    int IF_ACMPNE = 166; // -
    int GOTO = 167; // -
    int JSR = 168; // -
    int RET = 169; // visitVarInsn
    int TABLESWITCH = 170; // visiTableSwitchInsn
    int LOOKUPSWITCH = 171; // visitLookupSwitch
    int IRETURN = 172; // visitInsn
    int LRETURN = 173; // -
    int FRETURN = 174; // -
    int DRETURN = 175; // -
    int ARETURN = 176; // -
    int RETURN = 177; // -
    int GETSTATIC = 178; // visitFieldInsn
    int PUTSTATIC = 179; // -
    int GETFIELD = 180; // -
    int PUTFIELD = 181; // -
    int INVOKEVIRTUAL = 182; // visitMethodInsn
    int INVOKESPECIAL = 183; // -
    int INVOKESTATIC = 184; // -
    int INVOKEINTERFACE = 185; // -
    int INVOKEDYNAMIC = 186; // visitInvokeDynamicInsn
    int NEW = 187; // visitTypeInsn
    int NEWARRAY = 188; // visitIntInsn
    int ANEWARRAY = 189; // visitTypeInsn
    int ARRAYLENGTH = 190; // visitInsn
    int ATHROW = 191; // -
    int CHECKCAST = 192; // visitTypeInsn
    int INSTANCEOF = 193; // -
    int MONITORENTER = 194; // visitInsn
    int MONITOREXIT = 195; // -
     int WIDE = 196; // NOT VISITED
    int MULTIANEWARRAY = 197; // visitMultiANewArrayInsn
    int IFNULL = 198; // visitJumpInsn
    int IFNONNULL = 199; // -
     int GOTO_W = 200; // -
     int JSR_W = 201; // -
	
	String
			OBJECT_NAME = getClassName("java.lang.Object"),
			CLASS_NAME = getClassName("java.lang.Class"),
			STRING_NAME = getClassName("java.lang.String"),
			OBJECT_DESC = getClassDesc(OBJECT_NAME),
			_INIT_ = "<init>",
			_CLINIT_ = "<clinit>",
			VOID_METHOD_DESC = Type.getMethodDescriptor(Type.VOID_TYPE);
	
	Type
			TYPE_OBJECT = Type.getObjectType(OBJECT_NAME),
			TYPE_CLASS = Type.getObjectType(CLASS_NAME),
			TYPE_STRING = Type.getObjectType(STRING_NAME),
			TYPE_VOID = Type.getType(Void.class),
			TYPE_ENUM = Type.getType(Enum.class);
	
	static class DynamicVarInsnNode extends VarInsnNode {

		public DynamicVarInsnNode(int opcode, int var) {
			super(opcode, var);
		}
		
		public static void normalizationInsnList(InsnList list, int stackSize) {
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

	}
	
	@NotThreadSafe
	static final class OffsetCalculator implements Cloneable {
		
		protected final List<Type> types;
		protected boolean isStatic;
		protected int baseOffset, index, offset;
		
		public OffsetCalculator(Type... types) {
			this(true, types);
		}
		
		public OffsetCalculator(boolean isStaitc, Type... types) {
			this(isStaitc, 0, types);
		}
		
		public OffsetCalculator(boolean isStaitc, int baseOffset, Type... types) {
			this.types = Lists.newArrayList(types);
			this.isStatic = isStaitc;
			this.baseOffset = baseOffset;
			reset();
		}
		
		public OffsetCalculator reset() {
			index = -1;
			offset = baseOffset + (isStatic ? -1 : 0);
			return this;
		}
		
		public boolean hasNext() {
			return index < types.size() - 1;
		}
		
		public boolean hasPrev() {
			return index > 0;
		}
		
		public int next() {
			return offset(+1);
		}
		
		public int next(Type type) {
			types.add(type);
			return next();
		}
		
		public int prev() {
			return offset(-1);
		}
		
		public int nowIndex() {
			return index;
		}
		
		public int nowOffset() {
			return offset;
		}
		
		public OffsetCalculator skip(int n) {
			offset(n);
			return this;
		}
		
		public int offset(int n) {
			if (index + n > types.size())
				throw new ArrayIndexOutOfBoundsException(index + n);
			if (n == 0)
				return offset;
			int lastOffset = 0;
			if (n > 0) {
				for (int max = index + n; index < max; index++)
					offset += lastOffset = types.get(index + 1).getSize();
				return lastOffset > 1 ? offset - 1 : offset;
			} else {
				for (int min = index+ n; index > min; index--)
					offset -= lastOffset = types.get(index - 1).getSize();
				return offset;
			}
		}
		
		public Type nowType() {
			return types.get(index);
		}
		
		public int maxLength() {
			int result = isStatic ? 0 : 1;
			for (Type type : types)
				result += type.getSize();
			return result;
		}
		
		@Override
		public OffsetCalculator clone() {
			return new OffsetCalculator(types.toArray(new Type[0]));
		}
		
		public static OffsetCalculator fromMethodNode(MethodNode node) {
			return new OffsetCalculator((node.access & ACC_STATIC) == 0,Type.getMethodType(node.desc));
		}
		
	}
	
	static class MethodGenerator extends GeneratorAdapter {
		
		protected static final String LAMBDA_META_FACTORY_NAME = ASMHelper.getClassName("java.lang.invoke.LambdaMetafactory");
		
		protected static final Type
				TYPE_UNSAFE = Type.getObjectType(getClassName("sun.misc.Unsafe")),
				TYPE_REFLECTION_HELPER = Type.getObjectType(getClassName("index.alchemy.util.ReflectionHelper")),
				TYPE_LOOKUP = Type.getObjectType(getClassName("java.lang.invoke.MethodHandles$Lookup")),
				TYPE_METHOD_TYPE = Type.getObjectType(getClassName("java.lang.invoke.MethodType")),
				TYPE_METHOD_HANDLE = Type.getObjectType(getClassName("java.lang.invoke.MethodHandle")),
				TYPE_CALL_SITE = Type.getObjectType(getClassName("java.lang.invoke.CallSite")),
				TYPE_ALCHEMY_ENGINE = Type.getObjectType(getClassName("index.alchemy.core.AlchemyEngine")),
				TYPE_ALCHEMY_DEBUG = Type.getObjectType(getClassName("index.alchemy.core.debug.AlchemyDebug")),
				TYPE_TOOL = Type.getObjectType(getClassName("index.alchemy.util.Tool"));
		protected static final Method
				METHOD_ALLOCATE_INSTANCE = new Method("allocateInstance", TYPE_OBJECT, new Type[]{ TYPE_CLASS }),
				UNSAFE = new Method("unsafe", TYPE_UNSAFE, new Type[]{ }),
				LOOKUP = new Method("lookup", TYPE_LOOKUP, new Type[] { }),
				METHOD_TYPE = new Method("methodType", TYPE_METHOD_TYPE,  new Type[] { TYPE_CLASS, getArrayType(TYPE_CLASS) }),
				FIND_SPECIAL = new Method("findSpecial", TYPE_METHOD_HANDLE, new Type[] { TYPE_CLASS, TYPE_STRING, TYPE_METHOD_TYPE, TYPE_CLASS }),
				FIND_STATIC = new Method("findStatic", TYPE_METHOD_HANDLE, new Type[] { TYPE_CLASS, TYPE_STRING, TYPE_METHOD_TYPE }),
				FIND_VIRTUAL = new Method("findVirtual", TYPE_METHOD_HANDLE, new Type[] { TYPE_CLASS, TYPE_STRING, TYPE_METHOD_TYPE }),
				FIND_CONSTRUCTOR = new Method("findConstructor", TYPE_METHOD_HANDLE, new Type[] { TYPE_CLASS, TYPE_METHOD_TYPE }),
				PRINTLN = new Method("println", Type.VOID_TYPE, new Type[] { TYPE_OBJECT }),
				VALUE_OF = new Method("valueOf", TYPE_ENUM, new Type[] { TYPE_CLASS, TYPE_STRING }),
				FOR_NAME = new Method("forName", TYPE_CLASS, new Type[] { TYPE_STRING });
		protected static final Handle
				HANDLE_LAMBDA_META_FACTORY_META_FACTORY = new Handle(H_INVOKESTATIC, LAMBDA_META_FACTORY_NAME,
				"metafactory", Type.getMethodDescriptor(TYPE_CALL_SITE, TYPE_LOOKUP, TYPE_STRING, TYPE_METHOD_TYPE, TYPE_METHOD_TYPE,
				TYPE_METHOD_HANDLE, TYPE_METHOD_TYPE), false);
		
		protected static final sun.misc.Unsafe unsafe = $.unsafe();
		
		protected final Type methodType;
		
		protected Stack<Integer> localOffsetStack = new Stack<>();
		protected int nowLocalOffset = 0;

		public MethodGenerator(MethodVisitor mv, int access, String name, String desc) {
			super(ASM5, mv, access, name, desc);
			methodType = Type.getMethodType(desc);
		}
		
		public void pushLocalOffset(int offset) {
			localOffsetStack.push(offset);
		}
		
		public void popLocalOffset() {
			nowLocalOffset -= localOffsetStack.pop();
		}
		
		public int nowLocalOffset() {
			return nowLocalOffset;
		}
		
		public int getOffset(int index) {
			int result = firstLocal;
			for (int i = 0; i < index; i++) {
				int size = getLocalType(firstLocal + i).getSize(), offset = size - 1;
				result += size;
				if (offset != 0) {
					i += offset;
					index += offset;
				}
			}
			return result;
		}
		
		public void loadLocal(int local) {
			super.loadLocal(getOffset(local));
		}
		
		@Override
		public void loadLocal(int local, Type type) {
			super.loadLocal(getOffset(local), type);
		}
		
		@Override
		public void storeLocal(int local) {
			super.storeLocal(getOffset(local));
		}
		
		@Override
		public void storeLocal(int local, Type type) {
			super.storeLocal(getOffset(local), type);
		}
		
		@Override
		public void push(Type value) {
			if (value == null)
				pushNull();
			else if (isUnboxType(value))
				getStatic(getBoxType(value), "TYPE", TYPE_CLASS);
			else
				visitLdcInsn(value);
		}
		
		public void push(Class<?> clazz) {
			if (Modifier.isPublic(clazz.getModifiers()))
				push(Type.getType(clazz));
			else
				forName(Type.getType(clazz));
		}
		
		public void pushNull() {
			mv.visitInsn(Opcodes.ACONST_NULL);
		}
		
		public void forName(Type type) {
			if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
				push(ASMHelper.forName(type));
				invokeStatic(TYPE_CLASS, FOR_NAME);
			} else
				push(type);
		}
		
		public void valueOfEnum() {
			invokeStatic(TYPE_ENUM, VALUE_OF);
		}
		
		public void allocateInstance(Type type) {
			push(type);
			invokeStatic(TYPE_REFLECTION_HELPER, METHOD_ALLOCATE_INSTANCE);
		}
		
		public void loadUnsafe() {
			invokeStatic(TYPE_REFLECTION_HELPER, UNSAFE);
		}
		
		public void invokeInsn(int opcode, Type type, Method method, boolean itf) {
			String owner = type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName();
			mv.visitMethodInsn(opcode, owner, method.getName(), method.getDescriptor(), itf);
		}
		
		public void invokeSpecial(Type type, Method method, boolean itf) {
			invokeInsn(Opcodes.INVOKESPECIAL, type, method, itf);
		}
		
		public void invokeStatic(Type type, Method method, boolean itf) {
			invokeInsn(Opcodes.INVOKESTATIC, type, method, itf);
		}
		
		public void invokeLambda(String name, String ifaceMethodDesc, Type lambdaMethod, Handle lambdaHandle) {
			invokeDynamic(name, ifaceMethodDesc, HANDLE_LAMBDA_META_FACTORY_META_FACTORY, lambdaMethod, lambdaHandle, lambdaMethod);
		}
		
		public void invokePrintln(Type type) {
			box(type);
			invokeStatic(TYPE_ALCHEMY_DEBUG, PRINTLN);
		}
		
		public FieldNode findSpecialAndInvoke(Type owner, Type target, Method method, Runnable loadArgs) {
			return findMethodHandleAndInvoke(owner, target, method, FIND_SPECIAL, loadArgs);
		}
		
		public FieldNode findStaticAndInvoke(Type owner, Type target, Method method, Runnable loadArgs) {
			return findMethodHandleAndInvoke(owner, target, method, FIND_STATIC, loadArgs);
		}
		
		public FieldNode findVirtualAndInvoke(Type owner, Type target, Method method, Runnable loadArgs) {
			return findMethodHandleAndInvoke(owner, target, method, FIND_VIRTUAL, loadArgs);
		}
		
		public FieldNode findInterfaceAndInvoke(Type owner, Type target, Method method, Runnable loadArgs) {
			return findMethodHandleAndInvoke(owner, target, method, FIND_VIRTUAL, loadArgs);
		}
		
		public FieldNode findConstructorAndInvoke(Type owner, Type target, Method method, Runnable loadArgs) {
			return findMethodHandleAndInvoke(owner, target, method, FIND_CONSTRUCTOR, loadArgs);
		}
		
		protected FieldNode findMethodHandleAndInvoke(Type owner, Type target, Method method, Method findMethod, Runnable loadArgs) {
			Type returnType = findMethod.getReturnType();
			FieldNode fieldNode = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
						getStdName(target.getInternalName() + "_" + method.getName() + "_" + method.getDescriptor()), returnType.getDescriptor(), null, null);
			getStatic(owner, fieldNode.name, TYPE_METHOD_HANDLE);
			Label label = newLabel();
			ifNonNull(label);
			invokeStatic(TYPE_ALCHEMY_ENGINE, LOOKUP);
			forName(target);
			if (findMethod != FIND_CONSTRUCTOR)
				push(method.getName());
			Type args[] = method.getArgumentTypes();
			forName(method.getReturnType());
			push(args.length);
			newArray(TYPE_CLASS);
			int index = 0;
			for (Type arg : args) {
				dup();
				push(index++);
				forName(arg);
				arrayStore(TYPE_CLASS);
			}
			invokeStatic(TYPE_METHOD_TYPE, METHOD_TYPE);
			if (findMethod == FIND_SPECIAL)
				forName(target);
			invokeVirtual(TYPE_LOOKUP, findMethod);
			putStatic(owner, fieldNode.name, TYPE_METHOD_HANDLE);
			mark(label);
			getStatic(owner, fieldNode.name, TYPE_METHOD_HANDLE);
			loadArgs.run();
			if (findMethod != FIND_STATIC)
				args = ArrayUtils.add(args, 0, TYPE_OBJECT);
			invokeVirtual(TYPE_METHOD_HANDLE, new Method("invoke", method.getReturnType().getSort() == Type.OBJECT ? TYPE_OBJECT : method.getReturnType(), args));
			return fieldNode;
		}
		
		public static MethodGenerator fromMethodNode(MethodNode node) {
			return new MethodGenerator(node, node.access, node.name, node.desc);
		}
		
		public static MethodGenerator visitMethod(ClassVisitor visitor, int access, String name, String desc, String signature, String exceptions[]) {
			return new MethodGenerator(visitor.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
		}
		
	}
	
	static class StaticDeobfClassWriter extends ClassWriter {

		public StaticDeobfClassWriter(int flags) {
			super(flags);
		}
		
		@Override
		protected String getCommonSuperClass(String a, String b) {
			ClassNode aNode = getClassNode(a), bNode = getClassNode(b);
			Tool.checkNull(aNode, bNode);
			if (isInstance(aNode, bNode))
				return a;
			if (isInstance(bNode, aNode))
				return b;
			if (isInterface(aNode) || isInterface(bNode))
				return OBJECT_NAME;
			do
				aNode = getSuperClassNode(aNode);
			while (!isInstance(aNode, bNode));
			return DeobfuscatingRemapper.instance().mapType(aNode.name);
		}
		
	}
	
	static class ClassNameRemapper extends Remapper {
		
		protected final String srcName, newName;
		
		public ClassNameRemapper(String srcName, String newName) {
			this.srcName = getClassName(srcName);
			this.newName = getClassName(newName);
		}
		
		@Override
		public String map(String typeName) {
			return srcName.equals(typeName) ? newName : super.map(typeName);
		}
		
		public static byte[] changeName(byte data[], String srcName, String newName) {
			ClassWriter writer = newClassWriter(0);
			newClassReader(data).accept(new ClassRemapper(writer, new ClassNameRemapper(srcName, newName)), ClassReader.EXPAND_FRAMES);
			return writer.toByteArray();
		}
		
	}
	
	@NotThreadSafe
	static class NodeCopier {
		
		protected final Map<LabelNode, LabelNode> labelMap = new HashMap<LabelNode, LabelNode>() {
			
			private static final long serialVersionUID = 9185769782813777014L;

			@Override
			public LabelNode get(Object key) {
				LabelNode result = super.get(key);
				if (result == null)
					put((LabelNode) key, result = new LabelNode());
				return result;
			}
			
		};
		
		@SuppressWarnings("unchecked")
		public <T extends AbstractInsnNode> T copy(T node) {
			if (node == null)
				return node;
			return (T) node.clone(labelMap);
		}
		
		public static InsnList merge(InsnList...lists) {
			InsnList result = new InsnList();
			NodeCopier copier = new NodeCopier();
			Stream.of(lists)
				.map(InsnList::iterator)
				.flatMap(Tool::iteratorStream)
				.map(copier::copy)
				.forEach(result::add);
			return result;
		}
		
	}
	
	BiMap<Type, Type> PRIMITIVE_MAPPING = ImmutableBiMap.<Type, Type>builder().putAll($.getPrimitiveMapping().entrySet().stream()
			.collect(Collectors.toMap(FunctionHelper.map(Map.Entry::getKey, Type::getType), FunctionHelper.map(Map.Entry::getValue, Type::getType),
					FunctionHelper.end(), Maps::newHashMap))).build();
	
	static boolean isUnboxType(Type type) {
		return PRIMITIVE_MAPPING.containsKey(type);
	}
	
	static boolean isBoxType(Type type) {
		return PRIMITIVE_MAPPING.containsValue(type);
	}
	
	static Type getBoxType(Type type) {
		return PRIMITIVE_MAPPING.getOrDefault(type, type);
	}
	
	static Type getUnboxType(Type type) {
		return PRIMITIVE_MAPPING.inverse().getOrDefault(type, type);
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
	
	static AbstractInsnNode getIntNode(int value) {
		if (value <= 5 && -1 <= value)
			return new InsnNode(ICONST_M1 + value + 1);
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
			return new IntInsnNode(BIPUSH, value);
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
			return new IntInsnNode(SIPUSH, value);
		return new LdcInsnNode(value);
	}
	
	static String getStdName(String name) {
		return name.replace("[", "_L").replaceAll("[().;/]", "_");
	}
	
	static String getClassName(Class<?> clazz) {
		return getClassName(clazz.getName());
	}
	
	static String getClassName(String clazz) {
		return clazz.indexOf('L') == 0 && clazz.indexOf(';') == clazz.length() - 1 ?
				clazz.replaceFirst("L", "").replace('.', '/').replace(";", "") : clazz.replace('.', '/').replace(";", "");
	}
	
	static String getClassDesc(Class<?> clazz) {
		return Type.getDescriptor(clazz);
	}
	
	static String getClassDesc(String clazz) {
		return "L" + getClassName(clazz) + ";";
	}
	
	static String getClassSrcName(String name) {
		return getClassName(name).replace('/', '.');
	}
	
	static Type getArrayType(Type type) {
		return getArrayType(type, 1);
	}
	
	static Type getArrayType(Type type, int dim) {
		if (dim < 1)
			return type;
		if (dim == 1)
			return Type.getType("[" + type.getDescriptor());
		char array[] = new char[dim];
		Arrays.fill(array, '[');
		return Type.getType(new String(array) + type.getDescriptor());
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
	
	static void useGetter(FieldNode field, String owner, ClassNode clazz, Method proxy) {
		clazz.methods
			.stream()
			.filter(target -> !corresponding(target, proxy))
			.forEach(target -> useGetter(field, owner, target, proxy));
	}
	
	static void useGetter(FieldNode field, String owner, MethodNode target, Method proxy) {
		useProxy(field, owner, target, proxy, GETSTATIC, GETFIELD);
	}
	
	static void useSetter(FieldNode field, String owner, ClassNode clazz, Method proxy) {
		clazz.methods
			.stream()
			.filter(target -> !corresponding(target, proxy))
			.forEach(target -> useSetter(field, owner, target, proxy));
	}
	
	static void useSetter(FieldNode field, String owner, MethodNode target, Method proxy) {
		useProxy(field, owner, target, proxy, PUTSTATIC, PUTFIELD);
	}
	
	static void useProxy(FieldNode field, String owner, MethodNode target, Method proxy, int staticFieldOpcode, int nonStaticFieldOpcode) {
		boolean isStaic = (field.access & ACC_STATIC) != 0;
		int fieldOpcode = isStaic ? staticFieldOpcode : nonStaticFieldOpcode, methodOpcode = isStaic ? INVOKESTATIC : INVOKEVIRTUAL;
		for (ListIterator<AbstractInsnNode> iterator = target.instructions.iterator(); iterator.hasNext();) {
			AbstractInsnNode insn = iterator.next();
			if (insn.getOpcode() == fieldOpcode && corresponding(field, owner, (FieldInsnNode) insn))
				iterator.set(new MethodInsnNode(methodOpcode, owner, proxy.getName(), proxy.getDescriptor(), false));
		}
	}
	
	static boolean isSuperCall(MethodNode method, AbstractInsnNode insnNode, String owner) {
		return findSuperCall(method, owner) == insnNode;
	}
	
	static MethodInsnNode findSuperCall(MethodNode method, String owner) {
		int mark = 0;
		for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
			AbstractInsnNode insnNode = iterator.next();
			if (insnNode instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) insnNode;
				if (methodNode.name.equals(_INIT_) && methodNode.owner.equals(owner))
					if (mark == 0)
						return methodNode;
					else
						mark--;
			} else if (insnNode instanceof TypeInsnNode) {
				TypeInsnNode typeNode = (TypeInsnNode) insnNode;
				if (typeNode.getOpcode() == NEW && typeNode.desc.equals(owner))
					mark++;
			}
		}
		return null;
	}
	
	Function<String, byte[]> getClassByteArray = c -> null;
	
	@Nullable
	static byte[] getClassData(String name) {
		byte bytecode[] = null;
		try {
			bytecode = getClassByteArray.apply(name);
			if (bytecode == null)
				bytecode = IOUtils.toByteArray(ASMHelper.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class"));
		} catch (IOException e) { }
		return bytecode;
	}
	
	@Nullable
	static ClassNode getClassNode(String name) {
		if (Strings.isNullOrEmpty(name))
			return null;
		try {
			ClassReader reader = new ClassReader(getClassData(name));
			ClassNode result = new ClassNode();
			reader.accept(result, 0);
			return result;
		} catch (Exception e) { e.printStackTrace(); return null; }
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
				&& Objects.equal(owner, fieldInsn.owner);
	}
	
	static boolean corresponding(MethodNode method, String owner, MethodInsnNode methodInsn) {
		return Objects.equal(method.name, methodInsn.name)
				&& Objects.equal(owner, methodInsn.owner);
	}
	
	static boolean corresponding(Method method, String owner, MethodInsnNode methodInsn) {
		return Objects.equal(method.getName(), methodInsn.name)
				&& Objects.equal(owner, methodInsn.owner);
	}
	
	static boolean corresponding(MethodNode method, String ownerA, String ownerB, Method other) {
		return Objects.equal(method.name, other.getName())
				&& Objects.equal(ownerA, ownerB);
	}
	
	static boolean corresponding(MethodNode method, Method other) {
		return Objects.equal(method.name, other.getName());
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
		AbstractInsnNode prev = iterator.previous();
		int offset = getStackFrameOffset(prev, false);
		boolean isFirst = true;
		iterator.remove();
		while (offset != 0 || (!isFirst && prev instanceof MethodInsnNode && Type.getReturnType(((MethodInsnNode) prev).desc).getSize() != 0)) {
			if (!iterator.hasPrevious())
				throw new RuntimeException("Unable to get the previous node.");
			isFirst = false;
			prev = iterator.previous();
			offset += getStackFrameOffset(prev, true);
			iterator.remove();
		}
	}
	
	static boolean isLoadThis(ListIterator<AbstractInsnNode> iterator) {
		AbstractInsnNode prev = iterator.previous();
		int offset = getStackFrameOffset(prev, false);
		boolean isFirst = true;
		while (offset != 0 || (!isFirst && prev instanceof MethodInsnNode && Type.getReturnType(((MethodInsnNode) prev).desc).getSize() != 0)) {
			if (!iterator.hasPrevious())
				throw new RuntimeException("Unable to get the previous node.");
			isFirst = false;
			prev = iterator.previous();
			offset += getStackFrameOffset(prev, true);
		}
		return prev.getOpcode() == ALOAD && prev instanceof VarInsnNode && ((VarInsnNode) prev).var == 0;
	}
	
	int[] OPCODE_STACK_FRAME_OFFSET_MAPPING = {
			NOP, 0,
			ICONST_5, 1,
			LCONST_1, 2,
			FCONST_2, 1,
			DCONST_1, 2,
			LDC_W, 1,
			LDC2_W, 2,
			ILOAD, 1,
			LLOAD, 2,
			FLOAD, 1,
			DLOAD, 2,
			ILOAD_3, 1,
			LLOAD_3, 2,
			FLOAD_3, 1,
			DLOAD_3, 2,
			ALOAD_3, 1,
			IALOAD, -1,
			LALOAD, 0,
			FALOAD, -1,
			DALOAD, 0,
			SALOAD, -1,
			ISTORE, -1,
			LSTORE, -2,
			FSTORE, -1,
			DSTORE, -2,
			ISTORE_3, -1,
			LSTORE_3, -2,
			FSTORE_3, -1,
			DSTORE_3, -2,
			ASTORE_3, -1,
			IASTORE, -2,
			LASTORE, -3,
			FASTORE, -2,
			DASTORE, -3,
			SASTORE, -2,
			POP, -1,
			POP2, -2,
			DUP_X2, 1,
			DUP2_X2, 2,
			SWAP, 0,
			IADD, -1,
			LADD, -2,
			FADD, -1,
			DADD, -2,
			ISUB, -1,
			LSUB, -2,
			FSUB, -1,
			DSUB, -2,
			IMUL, -1,
			LMUL, -2,
			FMUL, -1,
			DMUL, -2,
			IDIV, -1,
			LDIV, -2,
			FDIV, -1,
			DDIV, -2,
			IREM, -1,
			LREM, -2,
			FREM, -1,
			DREM, -2,
			DNEG, 0,
			LUSHR, -1,
			IAND, -1,
			LAND, -2,
			IOR, -1,
			LOR, -2,
			IXOR, -1,
			LXOR, -2,
			I2S, 0,
			LCMP, -3,
			FCMPG, -1,
			DCMPG, -3
	};
	
	@Alpha
	@Unsafe(Unsafe.ASM_API)
	static int getStackFrameOffset(int opcode) {
		assert OPCODE_STACK_FRAME_OFFSET_MAPPING.length % 2 == 0;
		for (int i = 0; i < OPCODE_STACK_FRAME_OFFSET_MAPPING.length; i += 2) {
			if (opcode <= OPCODE_STACK_FRAME_OFFSET_MAPPING[i])
				return OPCODE_STACK_FRAME_OFFSET_MAPPING[i + 1];
		}
		return 0;
	}
	
	@Alpha
	@Unsafe(Unsafe.ASM_API)
	static int getStackFrameOffset(AbstractInsnNode insn, boolean caclReturn) {
		int opcode = insn.getOpcode();
		if (insn instanceof InsnNode)
			return getStackFrameOffset(insn.getOpcode());
		else if (insn instanceof VarInsnNode)
			return getStackFrameOffset(insn.getOpcode());
		else if (insn instanceof LdcInsnNode)
			return 1;
		else if (insn instanceof IntInsnNode)
			return 1;
		else if (insn instanceof TypeInsnNode) {
			if (opcode == NEW)
				return 1;
		} else if (insn instanceof MethodInsnNode) {
			MethodInsnNode method = (MethodInsnNode) insn;
			Type args[] = Type.getArgumentTypes(method.desc);
			int size = (opcode == INVOKESTATIC ? 0 : -1) + (caclReturn ? Type.getReturnType(method.desc).getSize() : 0);
			for (Type arg : args)
				size -= arg.getSize();
			return size;
		} else if (insn instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode method = (InvokeDynamicInsnNode) insn;
			Type args[] = Type.getArgumentTypes(method.desc);
			int size = caclReturn ? Type.getReturnType(method.desc).getSize() : 0;
			for (Type arg : args)
				size -= arg.getSize();
			return size;
		} else if (insn instanceof FieldInsnNode) {
			FieldInsnNode fieldInsn = (FieldInsnNode) insn;
			int size = Type.getType(fieldInsn.desc).getSize();
			switch (opcode) {
				case GETSTATIC:
					return size;
				case PUTSTATIC:
					return -size;
				case GETFIELD:
					return size - 1;
				case PUTFIELD:
					return -size - 1;
			}
		} 
		return 0;
	}
	
	static ClassNode requestMinVersion(ClassNode node, int minVersion) {
		node.version = Math.max(minVersion, node.version);
		return node;
	}
	
	static ClassReader newClassReader(byte data[]) {
		return new ClassReader(data);
	}
	
	static ClassNode newClassNode(byte data[]) {
		return newClassNode(data, 0);
	}
	
	static ClassNode newClassNode(ClassNode node) {
		ClassNode result = new ClassNode(ASM5);
		node.accept(result);
		return result;
	}
	
	static ClassNode newClassNode(byte data[], int flags) {
		ClassReader reader = newClassReader(data);
		ClassNode result = new ClassNode(ASM5);
		reader.accept(result, flags);
		return result;
	}
	
	static ClassReader newClassReader(String name) {
		try {
			return new ClassReader(name);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static ClassNode newClassNode(String name, int flags) {
		ClassReader reader = newClassReader(name);
		ClassNode result = new ClassNode(ASM5);
		reader.accept(result, flags);
		return result;
	}
	
	static ClassNode newClassNode(String name) {
		return newClassNode(name, 0);
	}
	
	static ClassWriter newClassWriter() {
		return newClassWriter(0);
	}
	
	static ClassWriter newClassWriter(int flags) {
		return new StaticDeobfClassWriter(flags);
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
	
	static Class<?> loadClass(Type type, ClassLoader loader) throws ClassNotFoundException {
		return Class.forName(type.getClassName(), false, loader);
	}
	
	static String forName(Type type) {
		return type.getSort() == Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName();
	}
	
	static boolean onlyByteCode() {
		return false;
	}
	
}
