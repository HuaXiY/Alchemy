package index.alchemy.core.asm.transformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;

import index.alchemy.api.IByteBufSerializable;
import index.alchemy.api.IShareableSerializable;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.$;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.FunctionHelper;
import index.alchemy.util.Tool;
import index.alchemy.util.TypeResolver;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.Side;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.commons.GeneratorAdapter.*;

public class TransformerRemote implements IClassTransformer {
	
	protected static final String
			SIDE_NAME = ASMHelper.getClassName("net.minecraftforge.fml.relauncher.Side"),
			ALWAYS_NAME = ASMHelper.getClassName("index.alchemy.util.Always"),
			BYTE_BUF_NAME = ASMHelper.getClassName("io.netty.buffer.ByteBuf"),
			I_MESSAGE_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.network.simpleimpl.IMessage"),
			I_MESSAGE_HANDLER_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler"),
			MESSAGE_CONTEXT_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.network.simpleimpl.MessageContext"),
			BYTE_BUF_UTILS_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.network.ByteBufUtils"),
			I_BYTE_BUF_SERIALIZABLE_NAME = ASMHelper.getClassName("index.alchemy.api.IByteBufSerializable"),
			ALCHEMY_NETWORK_HANDLER_NAME = ASMHelper.getClassName("index.alchemy.network.AlchemyNetworkHandler"),
			STREAM_NAME = ASMHelper.getClassName("java.util.stream.Stream"),
			PHASE_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.gameevent.TickEvent$Phase"),
			I_PHASE_RUNNABLE_NAME = ASMHelper.getClassName("index.alchemy.api.IPhaseRunnable"),
			ALCHEMY_EVENT_SYSTEM_NAME = ASMHelper.getClassName("index.alchemy.core.AlchemyEventSystem"),
			UNSAFE_NAME = ASMHelper.getClassName("sun.misc.Unsafe"),
			I_FORGE_REGISTRY_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.registry.IForgeRegistry"),
			I_FORGE_REGISTRY_ENTRY_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.registry.IForgeRegistryEntry"),
			GAME_REGISTRY_NAME = ASMHelper.getClassName("net.minecraftforge.fml.common.registry.GameRegistry"),
			I_SHAREABLE_SERIALIZABLE_NAME = ASMHelper.getClassName("index.alchemy.api.IShareableSerializable");
	protected static final Type
			TYPE_INT_ARRAY = ASMHelper.getArrayType(Type.INT_TYPE),
			TYPE_SIDE = Type.getObjectType(SIDE_NAME),
			TYPE_ALWAYS = Type.getObjectType(ALWAYS_NAME),
			TYPE_BYTE_BUF = Type.getObjectType(BYTE_BUF_NAME),
			TYPE_I_MESSAGE = Type.getObjectType(I_MESSAGE_NAME),
			TYPE_MESSAGE_CONTEXT = Type.getObjectType(MESSAGE_CONTEXT_NAME),
			TYPE_BYTE_BUF_UTILS = Type.getObjectType(BYTE_BUF_UTILS_NAME),
			TYPE_I_BYTE_BUF_SERIALIZABLE = Type.getObjectType(I_BYTE_BUF_SERIALIZABLE_NAME),
			TYPE_ALCHEMY_NETWORK_HANDLER = Type.getObjectType(ALCHEMY_NETWORK_HANDLER_NAME),
			TYPE_STREAM = Type.getObjectType(STREAM_NAME),
			TYPE_PHASE = Type.getObjectType(PHASE_NAME),
			TYPE_I_PHASE_RUNNABLE = Type.getObjectType(I_PHASE_RUNNABLE_NAME),
			TYPE_ALCHEMY_EVENT_SYSTEM = Type.getObjectType(ALCHEMY_EVENT_SYSTEM_NAME),
			TYPE_UNSAFE = Type.getObjectType(UNSAFE_NAME),
			TYPE_I_FORGE_REGISTRY = Type.getObjectType(I_FORGE_REGISTRY_NAME),
			TYPE_I_FORGE_REGISTRY_ENTRY = Type.getObjectType(I_FORGE_REGISTRY_ENTRY_NAME),
			TYPE_GAME_REGISTRY = Type.getObjectType(GAME_REGISTRY_NAME),
			TYPE_I_SHAREABLE_SERIALIZABLE  = Type.getObjectType(I_SHAREABLE_SERIALIZABLE_NAME);
	protected static final String
			GET_SIGE_DESC = Type.getMethodDescriptor(TYPE_SIDE),
			IO_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, TYPE_BYTE_BUF),
			MESSAGE_HANDLER_DESC = Type.getMethodDescriptor(TYPE_I_MESSAGE, TYPE_I_MESSAGE, TYPE_MESSAGE_CONTEXT);
	protected static final Method
			GET_SIDE = new Method("getSide", GET_SIGE_DESC),
			READ_UTF8_STRING = new Method("readUTF8String", ASMHelper.TYPE_STRING, new Type[]{ TYPE_BYTE_BUF }),
			WRITE_UTF8_STRING = new Method("writeUTF8String", Type.VOID_TYPE, new Type[]{ TYPE_BYTE_BUF, ASMHelper.TYPE_STRING }),
			DESERIALIZE = new Method("deserialize", Type.VOID_TYPE, new Type[]{ TYPE_BYTE_BUF }),
			SERIALIZE = new Method("serialize", Type.VOID_TYPE, new Type[]{ TYPE_BYTE_BUF }),
			SEND_MESSAGE = new Method("sendMessage", Type.VOID_TYPE, new Type[]{ TYPE_STREAM, TYPE_I_MESSAGE }),
			GET_PLAYER_FROM_CONTEXT = new Method("getPlayerFromContext", TYPE_STREAM, new Type[]{ TYPE_MESSAGE_CONTEXT }),
			RUN = new Method("run", Type.VOID_TYPE, new Type[]{ TYPE_PHASE }),
			ADD_DELAYED_RUNNABLE = new Method("addDelayedRunnable", Type.VOID_TYPE, new Type[]{ TYPE_I_PHASE_RUNNABLE, Type.INT_TYPE }),
			READ_REGISTRY_ENTRY = new Method("readRegistryEntry", TYPE_I_FORGE_REGISTRY_ENTRY, new Type[] { TYPE_BYTE_BUF, TYPE_I_FORGE_REGISTRY }),
			WRITE_REGISTRY_ENTRY = new Method("writeRegistryEntry", Type.VOID_TYPE, new Type[] { TYPE_BYTE_BUF, TYPE_I_FORGE_REGISTRY_ENTRY }),
			FIND_REGISTRY = new Method("findRegistry", TYPE_I_FORGE_REGISTRY, new Type[] { ASMHelper.TYPE_CLASS }),
			NAME = new Method("name", ASMHelper.TYPE_STRING, new Type[] { }),
			STATIC_DESERIALIZE = new Method("deserialize", ASMHelper.TYPE_OBJECT, new Type[]{ TYPE_BYTE_BUF });
	
	protected final MethodNode remoteNode;
	protected final Side side;
	protected final boolean always, sync;
	protected final Type ownerType, messageType, messageSrcType, args[];
	
	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		ClassNode node = ASMHelper.newClassNode(basicClass);
		for (MethodNode method : node.methods)
			if (method.name.equals(remoteNode.name) && method.desc.equals(remoteNode.desc)) {
				AlchemyTransformerManager.transform("<remote>" + name + "|" + transformedName + "#" + remoteNode.name + remoteNode.signature);
				MethodNode newMethod = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
				ASMHelper.MethodGenerator generator = ASMHelper.MethodGenerator.fromMethodNode(newMethod);
				Label ignoredSrouce = generator.newLabel();
				if (always) {
					generator.invokeStatic(TYPE_ALWAYS, GET_SIDE);
					generator.getStatic(TYPE_SIDE, side.name(), TYPE_SIDE);
					generator.ifCmp(ASMHelper.TYPE_OBJECT, EQ, ignoredSrouce);
				}
				generator.loadArg(0);
				generator.newInstance(messageType);
				generator.dup();
				generator.loadArgs(1, args.length);
				generator.invokeConstructor(messageType, new Method(ASMHelper._INIT_, Type.getMethodDescriptor(Type.VOID_TYPE, args)));
				generator.invokeStatic(TYPE_ALCHEMY_NETWORK_HANDLER, SEND_MESSAGE);
				if (always) {
					generator.returnValue();
					generator.mark(ignoredSrouce);
				}
				generator.endMethod();
				if (sync) {
					MethodNode lambda = new MethodNode(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$" + method.name,
							Type.getMethodDescriptor(Type.VOID_TYPE, ArrayUtils.add(Type.getArgumentTypes(method.desc), TYPE_PHASE)), null, null);
					node.methods.add(lambda);
					lambda.instructions = ASMHelper.NodeCopier.merge(method.instructions);
					if (Type.getReturnType(method.desc) != Type.VOID_TYPE)
						Tool.iteratorStream(lambda.instructions.iterator())
								.filter(InsnNode.class::isInstance)
								.map(InsnNode.class::cast)
								.filter(insn -> insn.getOpcode() == ARETURN)
								.peek(insn -> lambda.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC,
										ALCHEMY_NETWORK_HANDLER_NAME, SEND_MESSAGE.getName(), SEND_MESSAGE.getDescriptor(), false)))
								.peek(insn -> lambda.instructions.insertBefore(insn, new InsnNode(RETURN)))
								.forEach(lambda.instructions::remove);
					method.instructions.clear();
					ASMHelper.MethodGenerator srcMethodGenerator = ASMHelper.MethodGenerator.fromMethodNode(method);
					srcMethodGenerator.loadArgs();
					srcMethodGenerator.invokeLambda(RUN.getName(), Type.getMethodDescriptor(TYPE_I_PHASE_RUNNABLE, Type.getArgumentTypes(method.desc)),
							Type.getMethodType(RUN.getDescriptor()), new Handle(H_INVOKESTATIC, node.name, lambda.name, lambda.desc, false));
					srcMethodGenerator.push(0);
					srcMethodGenerator.invokeStatic(TYPE_ALCHEMY_EVENT_SYSTEM, ADD_DELAYED_RUNNABLE);
					srcMethodGenerator.returnValue();
					srcMethodGenerator.endMethod();
				}
				InsnList merge = ASMHelper.NodeCopier.merge(newMethod.instructions, method.instructions);
				method.instructions.clear();
				method.instructions.add(merge);
				break;
			}
		ASMHelper.requestMinVersion(node, V1_8).accept(writer);
		return writer.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage() throws ClassNotFoundException {
		AlchemyNetworkHandler.registerMessage((Class<T>) createMessage(ownerType, messageSrcType, args, remoteNode), side);
	}
	
	@Unsafe(Unsafe.ASM_API)
	public static Class<?> createMessage(Type ownerType, Type messageType, Type args[], MethodNode method) throws ClassNotFoundException {
		AlchemyTransformerManager.transform("<create>" + ownerType.getClassName() + "|" + messageType.getClassName() + "#" + method.name + method.signature);
		String name = messageType.getInternalName();
		Iterator<LocalVariableNode> iterator;
		ClassWriter cw = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		ASMHelper.MethodGenerator generator;
		List<Class<?>>types;
		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, name, null, ASMHelper.OBJECT_NAME, 
				new String[]{ I_MESSAGE_NAME, I_MESSAGE_HANDLER_NAME });
		cw.visitSource("TransformerRemote.java:166", "invoke: " + ownerType.getClassName() + "#" + method.name);
		{
			iterator = method.localVariables.iterator();
			iterator.next();
			for (Type arg : args)
				cw.visitField(ACC_PUBLIC | ACC_SYNTHETIC, iterator.next().name, arg.getDescriptor(), null, null);
		}
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, ASMHelper._INIT_,
					ASMHelper.VOID_METHOD_DESC, null, null);
			generator.loadThis();
			generator.invokeConstructor(ASMHelper.TYPE_OBJECT, new Method(ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC));
			generator.returnValue();
			generator.endMethod();
		}
		if (args.length > 0)
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, ASMHelper._INIT_,
					Type.getMethodDescriptor(Type.VOID_TYPE, args), null, null);
			generator.loadThis();
			generator.invokeConstructor(ASMHelper.TYPE_OBJECT, new Method(ASMHelper._INIT_, ASMHelper.VOID_METHOD_DESC));
			iterator = method.localVariables.iterator();
			iterator.next();
			for (int index = 0; index < args.length; index++) {
				generator.loadThis();
				generator.loadArg(index);
				generator.putField(messageType, iterator.next().name, args[index]);
			}
			generator.returnValue();
			generator.endMethod();
		}
		types = Lists.newLinkedList();
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, "fromBytes", IO_DESC, null, null);
			iterator = method.localVariables.iterator();
			iterator.next();
			for (int index = 0; index < args.length; index++) {
				generator.loadThis();
				fromBytes(generator, args[index], name, types, cw);
				generator.putField(messageType, iterator.next().name, args[index]);
			}
			generator.returnValue();
			generator.endMethod();
		}
		types = Lists.newLinkedList();
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, "toBytes", IO_DESC, null, null);
			iterator = method.localVariables.iterator();
			iterator.next();
			for (int index = 0; index < args.length; index++) {
				generator.loadThis();
				generator.getField(messageType, iterator.next().name, args[index]);
				toBytes(generator, args[index], name, types, cw);
			}
			generator.returnValue();
			generator.endMethod();
		}
		{
			generator = ASMHelper.MethodGenerator.visitMethod(cw, ACC_PUBLIC | ACC_SYNTHETIC, "onMessage", MESSAGE_HANDLER_DESC, null, null);
			generator.loadArg(1);
			generator.invokeStatic(TYPE_ALCHEMY_NETWORK_HANDLER, GET_PLAYER_FROM_CONTEXT);
			iterator = method.localVariables.iterator();
			iterator.next();
			for (int index = 0; index < args.length; index++) {
				generator.loadArg(0);
				generator.checkCast(messageType);
				generator.getField(messageType, iterator.next().name, args[index]);
			}
			generator.invokeStatic(ownerType, new Method(method.name, method.desc));
			Type returnType = Type.getReturnType(method.desc);
			if (returnType == Type.VOID_TYPE)
				generator.pushNull();
			else if (!returnType.equals(TYPE_I_MESSAGE)) {
				generator.pop();
				generator.pushNull();
			}
			generator.returnValue();
			generator.endMethod();
		}
		cw.visitEnd();
		try {
			return AlchemyEngine.getASMClassLoader().define(messageType.getClassName(), cw.toByteArray());
		} catch(Exception e) { throw AlchemyRuntimeException.onException(e); }
	}
	
	protected static void fromBytes(ASMHelper.MethodGenerator generator, Type type, String name, List<Class<?>> types, ClassVisitor visitor) throws ClassNotFoundException {
		final int dim = type.getSort() != Type.ARRAY ? 0 : type.getDimensions();
		if (dim > 0) {
			Label label = generator.newLabel(), jump = generator.newLabel();
			generator.loadArg(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("readByte", Type.BYTE_TYPE, new Type[]{ }));
			generator.ifZCmp(EQ, label);
			int index = generator.nowLocalOffset(), max = index + 1, array = max + 1;
			Type eleType = ASMHelper.getArrayType(type.getElementType(), dim - 1);
			generator.push(-1);
			generator.storeLocal(index, Type.INT_TYPE);
			generator.loadArg(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("readInt", Type.INT_TYPE, new Type[]{ }));
			generator.dup();
			generator.storeLocal(max, Type.INT_TYPE);
			generator.newArray(eleType);
			generator.storeLocal(array, type);
			Label start = generator.mark(), end = generator.newLabel();
			generator.loadLocal(index);
			generator.push(1);
			generator.math(ADD, Type.INT_TYPE);
			generator.dup();
			generator.loadLocal(max);
			generator.ifICmp(GE, end);
			generator.dup();
			generator.storeLocal(index);
			generator.loadLocal(array);
			generator.swap();
			generator.pushLocalOffset(array - generator.nowLocalOffset() + 1);
			fromBytes(generator, eleType, name, types, visitor);
			generator.popLocalOffset();
			generator.arrayStore(eleType);
			generator.goTo(start);
			generator.mark(end);
			generator.pop();
			generator.loadLocal(array);
			generator.goTo(jump);
			generator.mark(label);
			generator.pushNull();
			generator.mark(jump);
		} else
			fromBytesNonArray(generator, type, name, types, visitor);
	}
	
	protected static void fromBytesNonArray(ASMHelper.MethodGenerator generator, Type type, String name, List<Class<?>> types, ClassVisitor visitor) throws ClassNotFoundException {
		Type unboxType = ASMHelper.getUnboxType(type);
		if (ASMHelper.isUnboxType(unboxType)) {
			generator.loadArg(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("read" + WordUtils.capitalize(unboxType.getClassName()), unboxType, new Type[]{ }));
			if (unboxType != type)
				generator.box(unboxType);
		} else {
			Label label = generator.newLabel(), jump = generator.newLabel();
			generator.loadArg(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("readByte", Type.BYTE_TYPE, new Type[]{ }));
			generator.ifZCmp(EQ, label);
			if (type.equals(ASMHelper.TYPE_STRING)) {
				generator.loadArg(0);
				generator.invokeStatic(TYPE_BYTE_BUF_UTILS, READ_UTF8_STRING);
			} else {
				Class<?> clazz = ASMHelper.loadClass(type, AlchemyEngine.getLaunchClassLoader());
				if ($.isInstance(Enum.class, clazz)) {
					generator.push(type);
					generator.loadArg(0);
					generator.invokeStatic(TYPE_BYTE_BUF_UTILS, READ_UTF8_STRING);
					generator.valueOfEnum();
					generator.checkCast(type);
				} else if ($.isInstance(IForgeRegistryEntry.class, clazz)) {
					generator.loadArg(0);
					generator.push(Type.getType(TypeResolver.resolveRawArguments(IForgeRegistryEntry.class, clazz)[0]));
					generator.invokeStatic(TYPE_GAME_REGISTRY, FIND_REGISTRY);
					generator.invokeStatic(TYPE_BYTE_BUF_UTILS, READ_REGISTRY_ENTRY);
					generator.checkCast(type);
				} else if ($.isInstance(IShareableSerializable.class, clazz)) {
					generator.loadArg(0);
					generator.invokeStatic(TYPE_I_SHAREABLE_SERIALIZABLE, STATIC_DESERIALIZE);
					generator.checkCast(type);
				} else if ($.isInstance(IByteBufSerializable.class, clazz)) {
					generator.allocateInstance(type);
					generator.dup();
					generator.loadArg(0);
					generator.invokeInterface(TYPE_I_BYTE_BUF_SERIALIZABLE, DESERIALIZE);
				} else {
					generator.loadArg(0);
					generator.invokeStatic(Type.getObjectType(name), new Method("fromBytes_" + clazz.getName().replace('.', '_'), type, new Type[] { TYPE_BYTE_BUF }));
					if (!types.contains(clazz)) {
						types.add(clazz);
						ASMHelper.MethodGenerator subGenerator = ASMHelper.MethodGenerator.visitMethod(visitor, ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
								"fromBytes_" + clazz.getName().replace('.', '_'), Type.getMethodDescriptor(type, TYPE_BYTE_BUF), null, null);
						subGenerator.allocateInstance(type);
						subGenerator.checkCast(type);
						for (Field field : clazz.getDeclaredFields()) {
							if (shouldNotSerialization(field))
								continue;
							Type fieldType = Type.getType(field.getType());
							boolean isUnbox = ASMHelper.isUnboxType(fieldType);
							subGenerator.dup();
							if (Modifier.isFinal(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
								long offset = AlchemyEngine.unsafe().objectFieldOffset(field);
								subGenerator.loadUnsafe();
								subGenerator.swap();
								subGenerator.push(offset);
								fromBytes(subGenerator, fieldType, name, types, visitor);
								subGenerator.invokeVirtual(TYPE_UNSAFE, new Method(isUnbox ?
										"put" + WordUtils.capitalize(fieldType.getClassName()) : "putObject",
										Type.getMethodDescriptor(Type.VOID_TYPE, ASMHelper.TYPE_OBJECT, Type.LONG_TYPE, isUnbox ? fieldType : ASMHelper.TYPE_OBJECT)));
							} else {
								fromBytes(subGenerator, fieldType, name, types, visitor);
								subGenerator.putField(type, field.getName(), fieldType);
							}
						}
						subGenerator.returnValue();
						subGenerator.endMethod();
					}
				}
			}
			generator.goTo(jump);
			generator.mark(label);
			generator.pushNull();
			generator.mark(jump);
		}
	}
	
	protected static void toBytes(ASMHelper.MethodGenerator generator, Type type, String name, List<Class<?>> types, ClassVisitor visitor) throws ClassNotFoundException {
		final int dim = type.getSort() != Type.ARRAY ? 0 : type.getDimensions();
		if (dim > 0) {
			Label label = generator.newLabel(), jump = generator.newLabel();
			generator.dup();
			generator.ifNull(label);
			generator.loadArg(0);
			generator.push(1);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("writeByte", TYPE_BYTE_BUF, new Type[]{ Type.INT_TYPE }));
			generator.pop();
			int index = generator.nowLocalOffset(), array = index + 1, max = array + 1;
			Type eleType = ASMHelper.getArrayType(type.getElementType(), dim - 1);
			generator.push(-1);
			generator.storeLocal(index, Type.INT_TYPE);
			generator.dup();
			generator.storeLocal(array, type);
			generator.arrayLength();
			generator.dup();
			generator.loadArg(0);
			generator.swap();
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("writeInt", TYPE_BYTE_BUF, new Type[]{ Type.INT_TYPE }));
			generator.pop();
			generator.storeLocal(max, Type.INT_TYPE);
			Label start = generator.mark(), end = generator.newLabel();
			generator.loadLocal(index);
			generator.push(1);
			generator.math(ADD, Type.INT_TYPE);
			generator.dup();
			generator.loadLocal(max);
			generator.ifICmp(GE, end);
			generator.dup();
			generator.storeLocal(index);
			generator.loadLocal(array);
			generator.swap();
			generator.arrayLoad(eleType);
			generator.pushLocalOffset(array - generator.nowLocalOffset() + 1);
			toBytes(generator, eleType, name, types, visitor);
			generator.popLocalOffset();
			generator.goTo(start);
			generator.mark(end);
			generator.pop();
			generator.goTo(jump);
			generator.mark(label);
			generator.pop();
			generator.loadArg(0);
			generator.push(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("writeByte", TYPE_BYTE_BUF, new Type[]{ Type.INT_TYPE }));
			generator.pop();
			generator.mark(jump);
		} else
			toBytesNonArray(generator, type, name, types, visitor);
	}
	
	protected static void toBytesNonArray(ASMHelper.MethodGenerator generator, Type type, String name, List<Class<?>> types, ClassVisitor visitor) throws ClassNotFoundException {
		Type unboxType = ASMHelper.getUnboxType(type);
		if (ASMHelper.isUnboxType(unboxType)) {
			generator.loadArg(0);
			generator.swap(type, TYPE_BYTE_BUF);
			if (unboxType != type)
				generator.unbox(unboxType);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("write" + WordUtils.capitalize(unboxType.getClassName()),
					TYPE_BYTE_BUF, new Type[]{ unboxType == Type.BYTE_TYPE || unboxType == Type.SHORT_TYPE ? Type.INT_TYPE : unboxType }));
			generator.pop();
		} else {
			Label label = generator.newLabel(), jump = generator.newLabel();
			generator.dup();
			generator.ifNull(label);
			generator.loadArg(0);
			generator.push(1);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("writeByte", TYPE_BYTE_BUF, new Type[]{ Type.INT_TYPE }));
			generator.pop();
			if (type.equals(ASMHelper.TYPE_STRING)) {
				generator.loadArg(0);
				generator.swap();
				generator.invokeStatic(TYPE_BYTE_BUF_UTILS, WRITE_UTF8_STRING);
			} else {
				Class<?> clazz = ASMHelper.loadClass(type, AlchemyEngine.getLaunchClassLoader());
				if ($.isInstance(Enum.class, clazz)) {
					generator.loadArg(0);
					generator.swap();
					generator.invokeVirtual(ASMHelper.TYPE_ENUM, NAME);
					generator.invokeStatic(TYPE_BYTE_BUF_UTILS, WRITE_UTF8_STRING);
				} else if ($.isInstance(IForgeRegistryEntry.class, clazz)) {
					generator.loadArg(0);
					generator.swap();
					generator.invokeStatic(TYPE_BYTE_BUF_UTILS, WRITE_REGISTRY_ENTRY);
				} else if ($.isInstance(IShareableSerializable.class, clazz)) {
					generator.loadArg(0);
					generator.invokeInterface(TYPE_I_SHAREABLE_SERIALIZABLE, SERIALIZE);
				} else if ($.isInstance(IByteBufSerializable.class, clazz)) {
					generator.loadArg(0);
					generator.invokeInterface(TYPE_I_BYTE_BUF_SERIALIZABLE, SERIALIZE);
				} else {
					generator.loadArg(0);
					generator.swap();
					generator.invokeStatic(Type.getObjectType(name), new Method("toBytes_" + clazz.getName().replace('.', '_'),
							Type.VOID_TYPE, new Type[] { TYPE_BYTE_BUF, type }));
					if (!types.contains(clazz)) {
						types.add(clazz);
						ASMHelper.MethodGenerator subGenerator = ASMHelper.MethodGenerator.visitMethod(visitor,
								ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, "toBytes_" + clazz.getName().replace('.', '_'),
								Type.getMethodDescriptor(Type.VOID_TYPE, TYPE_BYTE_BUF, type), null, null);
						subGenerator.loadArg(1);
						for (Field field : clazz.getDeclaredFields()) {
							if (shouldNotSerialization(field))
								continue;
							Type fieldType = Type.getType(field.getType());
							boolean isUnbox = ASMHelper.isUnboxType(fieldType);
							subGenerator.dup();
							if (Modifier.isFinal(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
								long offset = AlchemyEngine.unsafe().objectFieldOffset(field);
								subGenerator.loadUnsafe();
								subGenerator.swap();
								subGenerator.push(offset);
								subGenerator.invokeVirtual(TYPE_UNSAFE, new Method(isUnbox ?
										"get" + WordUtils.capitalize(fieldType.getClassName()) : "getObject",
										Type.getMethodDescriptor(isUnbox ? fieldType : ASMHelper.TYPE_OBJECT, ASMHelper.TYPE_OBJECT, Type.LONG_TYPE)));
								if (!isUnbox)
									subGenerator.checkCast(fieldType);
							} else
								subGenerator.getField(type, field.getName(), fieldType);
							toBytes(subGenerator, fieldType, name, types, visitor);
						}
						subGenerator.pop();
						subGenerator.returnValue();
						subGenerator.endMethod();
					}
				}
			}
			generator.goTo(jump);
			generator.mark(label);
			generator.pop();
			generator.loadArg(0);
			generator.push(0);
			generator.invokeVirtual(TYPE_BYTE_BUF, new Method("writeByte", TYPE_BYTE_BUF, new Type[]{ Type.INT_TYPE }));
			generator.pop();
			generator.mark(jump);
		}
	}
	
	protected static boolean shouldNotSerialization(Field field) {
		return Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers());
	}
	
	protected static String getUniqueName(String clazzName, String methodName, String type) {
		return String.format(
				"%s_%d_%s_%s_%s",
				AlchemyEngine.getASMClassLoader().getClass().getName(),
				AlchemyEngine.getASMClassLoader().nextId(),
				clazzName.replace('.', '_'),
				methodName,
				type
		);
	}
	
	public TransformerRemote(MethodNode remoteNode, Side side, boolean always, boolean sync, String clazzName) {
		this.remoteNode = remoteNode;
		this.side = side;
		this.always = always;
		this.sync = sync;
		ownerType = Type.getObjectType(clazzName);
		Type args[] = Type.getArgumentTypes(remoteNode.desc);
		args = ArrayUtils.subarray(args, 1, args.length);
		this.args = args;
		messageSrcType = Type.getObjectType(ASMHelper.getClassName(getUniqueName(ownerType.getClassName(), remoteNode.name, "NetworkMessage")));
		messageType = Type.getObjectType(ASMHelper.getClassName(AlchemyEngine.ASMClassLoader.DYNAMIC_PACKAGE_NAME + messageSrcType.getClassName()));
		AlchemyModLoader.addFMLEventCallback(FMLPreInitializationEvent.class, FunctionHelper.onThrowableRunnable(this::registerMessage,
				FunctionHelper::rethrow));
	}

}
