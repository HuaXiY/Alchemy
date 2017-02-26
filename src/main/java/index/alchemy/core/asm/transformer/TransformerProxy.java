package index.alchemy.core.asm.transformer;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.project.version.annotation.Alpha;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Alpha
public class TransformerProxy implements IClassTransformer {
	
	protected final int opcode;
	protected final String target, srgName, desc;
	protected final MethodNode proxyMethod;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName = this.srgName;
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled())
			srgName = DeobfuscatingRemapper.instance().mapMethodName(transformedName, this.srgName, desc);
		AlchemyTransformerManager.transform("<proxy>" + name + "|" + transformedName + "#" + proxyMethod.name + proxyMethod.signature
				+ "\n->  " + target + "#" + srgName + desc);
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		MethodNode oldMethod = null, newMethod = new MethodNode(proxyMethod.access, proxyMethod.name, proxyMethod.desc, proxyMethod.signature,
				proxyMethod.exceptions.toArray(new String[proxyMethod.exceptions.size()]));
		for (MethodNode method : node.methods)
			if (method.name.equals(proxyMethod.name) && method.desc.equals(proxyMethod.desc)) {
				oldMethod = method;
				GeneratorAdapter adapter = new GeneratorAdapter(newMethod, method.access, method.name, method.desc);
				Type owner = Type.getType(ASMHelper.getClassDesc(target));
				Method targetMethod = new Method(srgName, desc);
				switch (opcode) {
					case INVOKESTATIC:
						adapter.loadArgs();
						adapter.invokeStatic(owner, targetMethod);
						break;
					case INVOKEVIRTUAL:
						adapter.loadArgs();
						adapter.invokeVirtual(owner, targetMethod);
						break;
					case INVOKESPECIAL:
						adapter.invokeStatic(Type.getType(AlchemyEngine.class), new Method("lookup",
								"()Ljava/lang/invoke/MethodHandles$Lookup;"));
						adapter.push(srgName);
						Type returnType = Type.getReturnType(desc), args[] = Type.getArgumentTypes(desc);
						adapter.visitFieldInsn(GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
						adapter.push(args.length);
						adapter.newArray(Type.getType(Class.class));
						for (Type arg : args) {
							adapter.dup();
							adapter.push(arg);
							adapter.arrayStore(Type.getType(Class.class));
						}
						adapter.invokeStatic(Type.getType(MethodType.class), new Method("methodType",
								"(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"));
						adapter.push(owner);
						adapter.dupX2();
						adapter.invokeVirtual(Type.getType(MethodHandles.Lookup.class), new Method("findSpecial",
								"(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"));
						adapter.loadArgs();
						adapter.invokeVirtual(Type.getType(MethodHandle.class), new Method("invoke", proxyMethod.desc));
						break;
					case INVOKEINTERFACE:
						adapter.loadArgs();
						adapter.invokeInterface(owner, targetMethod);
						break;
					default:
						AlchemyRuntimeException.onException(new RuntimeException("Unsupported opcode: " + opcode));
				}
				adapter.returnValue();
				adapter.endMethod();
				break;
			}
		node.methods.remove(oldMethod);
		node.methods.add(newMethod);
		node.accept(writer);
		return writer.toByteArray();
	}
	

	public TransformerProxy(MethodNode proxyMethod, int opcode, String target, String srgName) {
		proxyMethod.accept(this.proxyMethod = new MethodNode(proxyMethod.access, proxyMethod.name, proxyMethod.desc,
				proxyMethod.signature, proxyMethod.exceptions.toArray(new String[proxyMethod.exceptions.size()])));
		this.opcode = opcode;
		this.target = target;
		this.srgName = srgName;
		this.desc = getDesc(opcode, proxyMethod.desc);
	}
	
	protected static String getDesc(int opcode, String desc) {
		if (opcode != INVOKESTATIC) {
			Type args[] = Type.getArgumentTypes(desc);
			args = ArrayUtils.subarray(args, 1, args.length);
			return Type.getMethodDescriptor(Type.getReturnType(desc), args);
		}
		return desc;
	}

}
