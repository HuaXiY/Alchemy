package index.alchemy.core.asm.transformer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

@Omega
public final class TransformerHook implements IClassTransformer {
	
	protected static final String
		HOOK_RESULT_NAME = ASMHelper.getClassName("index.alchemy.api.annotation.Hook$Result"),
		TOOL_NAME = ASMHelper.getClassName("index.alchemy.util.Tool"),
		VOID_NAME = ASMHelper.getClassName("java.lang.Void"),
		MAP_NAME = ASMHelper.getClassDesc("java.util.Map");
	protected static final Type
		TYPE_HOOK_RESULT = Type.getObjectType(HOOK_RESULT_NAME),
		TYPE_TOOL = Type.getObjectType(TOOL_NAME),
		TYPE_VOID = Type.getObjectType(VOID_NAME),
		TYPE_MAP = Type.getObjectType(MAP_NAME);
	protected static final Method
		GET = new Method("get", ASMHelper.TYPE_OBJECT, new Type[] { ASMHelper.TYPE_OBJECT }),
		CONTAINS_KEY = new Method("containsKey", Type.BOOLEAN_TYPE, new Type[] { ASMHelper.TYPE_OBJECT });
	
	protected final MethodNode hookMethod;
	protected final String hookSrc, srgName;
	protected final boolean isStatic, stackFlag;
	protected final Hook.Type type;
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName = AlchemyEngine.isRuntimeDeobfuscationEnabled() ? this.srgName : DeobfuscatingRemapper.instance().mapMethodName(transformedName, this.srgName, "");
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassNode node = ASMHelper.newClassNode(basicClass);
		Predicate<MethodNode> methodChecker;
		switch (srgName) {
			case "<any>":
			{
				methodChecker = method -> true;
				break;
			}
			case "<type>":
			{
				methodChecker = method -> checkMethodNode(method);
				break;
			}
			default:
			{
				methodChecker = method -> method.name.equals(srgName) && checkMethodNode(method);
			}
		}
		for (MethodNode method : node.methods)
			if (methodChecker.test(method)) {
				AlchemyTransformerManager.transform("<hook>" + name + "|" + transformedName + "#" + srgName + method.desc + "\n->  " +
						ASMHelper.getClassSrcName(hookSrc) + "#" + hookMethod.name + hookMethod.desc);
				Type returnType = Type.getReturnType(method.desc);
				int returnOpcode = ASMHelper.getReturnOpcode(returnType);
				insn_node: for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					if (type == Hook.Type.HEAD || ASMHelper.isOverOpcode(insn.getOpcode())) {
						MethodNode injectMethod = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
						ASMHelper.MethodGenerator generator = ASMHelper.MethodGenerator.fromMethodNode(injectMethod);
						if ("<any>".equals(srgName)) {
							Type args[] = Type.getArgumentTypes(hookMethod.desc);
							int length = args.length, target = Type.getArgumentTypes(method.desc).length;
							if (length > 0 && (method.access & ACC_STATIC) == 0) {
								generator.loadThis();
								length--;
							}
							if (length > 0)
								for (int i = 0; i < length; i++) {
									if (i < target) {
										generator.loadArg(i);
										generator.checkCast(args[i]);
									} else
										generator.loadVoid();
								}
						} else {
							if (!isStatic)
								generator.loadThis();
							generator.loadArgs();
						}
						generator.invokeStatic(Type.getObjectType(hookSrc), new Method(hookMethod.name, hookMethod.desc));
						if (Type.getReturnType(hookMethod.desc).equals(Type.getType(Hook.Result.class))) {
							generator.dup();
							generator.getField(TYPE_HOOK_RESULT, "result", ASMHelper.TYPE_OBJECT);
							generator.dup();
							generator.getStatic(TYPE_TOOL, "VOID", TYPE_VOID);
							Label label = generator.newLabel();
							generator.ifZCmp(IF_ACMPEQ, label);
							switch (returnOpcode) {
								case IRETURN:
								case LRETURN:
								case FRETURN:
								case DRETURN:
									generator.checkCast(ASMHelper.getBoxType(returnType));
									generator.unbox(returnType);
									break;
								case ARETURN:
									generator.checkCast(returnType);
									break;
								case RETURN:
									generator.pop();
									break;
								default:
							}
							generator.returnValue();
							generator.mark(label);
							generator.pop();
							if (stackFlag) {
								label = generator.newLabel();
								generator.getField(TYPE_HOOK_RESULT, "stackContext", TYPE_MAP);
								generator.dup();
								generator.ifNull(label);
								for (ASMHelper.OffsetCalculator calculator = ASMHelper.OffsetCalculator.fromMethodNode(method); calculator.hasNext(); calculator.next()) {
									generator.dup();
									generator.push(calculator.nowIndex());
									generator.box(Type.INT_TYPE);
									generator.invokeInterface(TYPE_MAP, CONTAINS_KEY);
									Label ifNull = generator.newLabel(), nonNull = generator.newLabel();
									generator.ifZCmp(ASMHelper.MethodGenerator.EQ, ifNull);
									generator.invokeInterface(TYPE_MAP, GET);
									generator.checkCast(ASMHelper.getBoxType(calculator.nowType()));
									if (ASMHelper.isUnboxType(calculator.nowType()))
										generator.unbox(ASMHelper.getUnboxType(calculator.nowType()));
									generator.storeArg(calculator.nowIndex());
									generator.goTo(nonNull);
									generator.mark(ifNull);
									generator.pop();
									generator.mark(nonNull);
								}
								generator.mark(label);
							}
							generator.pop();
						}
						switch (type) {
							case TAIL:
								if (insn.getPrevious() != null)
									method.instructions.insert(insn.getPrevious(), injectMethod.instructions);
								else
									method.instructions.insert(injectMethod.instructions);
								break;
							case HEAD:
								method.instructions.insert(injectMethod.instructions);
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
		Iterator<Type> hookMethodTypes = Arrays.asList(Type.getArgumentTypes(hookMethod.desc)).iterator(),
				srcMethodTypes = Arrays.asList(Type.getArgumentTypes(method.desc)).iterator();
		if (!isStatic)
			hookMethodTypes.next();
		while (hookMethodTypes.hasNext())
			if (!srcMethodTypes.hasNext() || !hookMethodTypes.next().equals(srcMethodTypes.next()))
				return false;
		return !srcMethodTypes.hasNext();
	}
	
	public static boolean shouldMarkStack(MethodNode methodNode) {
		for (Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext();) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof FieldInsnNode) {
				FieldInsnNode field = (FieldInsnNode) insn;
				if (field.getOpcode() == PUTFIELD && field.name.equals("stackContext") &&
						field.owner.equals(HOOK_RESULT_NAME))
					return true;
			}
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode method = (MethodInsnNode) insn;
				if (method.name.equals("operationStack") && method.owner.equals(HOOK_RESULT_NAME))
					return true;
			}
		}
		return false;
	}

	public TransformerHook(MethodNode hookMethod, String hookSrc, String srgName, boolean isStatic, Hook.Type type) {
		hookMethod.accept(this.hookMethod = new MethodNode(hookMethod.access, hookMethod.name, hookMethod.desc,
				hookMethod.signature, hookMethod.exceptions.toArray(new String[hookMethod.exceptions.size()])));
		this.hookSrc = hookSrc;
		this.srgName = srgName;
		this.isStatic = isStatic;
		this.stackFlag = shouldMarkStack(hookMethod);
		this.type = type;
	}

}
