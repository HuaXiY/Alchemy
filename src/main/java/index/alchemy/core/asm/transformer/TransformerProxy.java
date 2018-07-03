package index.alchemy.core.asm.transformer;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.DeobfuscatingRemapper;
import index.project.version.annotation.Alpha;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

@Alpha
public class TransformerProxy implements IClassTransformer {
	
	protected final int opcode;
	protected final boolean useHandle;
	protected final String target, srgName, desc;
	protected final MethodNode proxyMethod;

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String srgName = this.srgName;
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled())
			srgName = DeobfuscatingRemapper.instance().mapMethodName(transformedName, this.srgName, desc);
		AlchemyTransformerManager.transform("<proxy>" + name + "|" + transformedName + "#" + proxyMethod.name + proxyMethod.desc
				+ "\n->  " + target + "#" + srgName + desc);
		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		ClassNode node = ASMHelper.newClassNode(basicClass);
		for (MethodNode method : node.methods)
			if (method.name.equals(proxyMethod.name) && method.desc.equals(proxyMethod.desc)) {
				method.access &= ~ACC_NATIVE;
				ASMHelper.MethodGenerator generator = ASMHelper.MethodGenerator.fromMethodNode(method);
				Type owner = Type.getObjectType(ASMHelper.getClassName(transformedName)), target = Type.getObjectType(ASMHelper.getClassName(this.target));
				Method targetMethod = new Method(srgName, desc);
				switch (opcode) {
					case INVOKESTATIC:
						if (useHandle)
							node.fields.add(generator.findStaticAndInvoke(owner, target, targetMethod, generator::loadArgs));
						else {
							generator.loadArgs();
							generator.invokeStatic(target, targetMethod);
						}
						break;
					case INVOKEVIRTUAL:
						if (useHandle)
							node.fields.add(generator.findVirtualAndInvoke(owner, target, targetMethod, generator::loadArgs));
						else {
							generator.loadArgs();
							generator.invokeVirtual(target, targetMethod);
						}
						break;
					case INVOKESPECIAL:
						if (useHandle)
							node.fields.add(generator.findSpecialAndInvoke(owner, target, targetMethod, generator::loadArgs));
						else {
							generator.loadArgs();
							generator.invokeSpecial(target, targetMethod);
						}
						break;
					case INVOKEINTERFACE:
						if (useHandle)
							node.fields.add(generator.findInterfaceAndInvoke(owner, target, targetMethod, generator::loadArgs));
						else {
							generator.loadArgs();
							generator.invokeInterface(target, targetMethod);
						}
						break;
					case NEW:
						if (useHandle)
							node.fields.add(generator.findConstructorAndInvoke(owner, target, targetMethod, generator::loadArgs));
						else {
							generator.newInstance(target);
							generator.dup();
							generator.loadArgs();
							generator.invokeConstructor(target, new Method(ASMHelper._INIT_, desc));
						}
						break;
					default:
						throw new RuntimeException("Unsupported opcode: " + opcode);
				}
				generator.returnValue();
				generator.endMethod();
				break;
			}
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public TransformerProxy(MethodNode proxyMethod, int opcode, boolean useHandle, String target, String srgName) {
		proxyMethod.accept(this.proxyMethod = new MethodNode(proxyMethod.access, proxyMethod.name, proxyMethod.desc,
				proxyMethod.signature, proxyMethod.exceptions.toArray(new String[proxyMethod.exceptions.size()])));
		this.opcode = opcode;
		this.useHandle = useHandle;
		this.target = target;
		this.srgName = srgName;
		this.desc = getDesc(opcode, proxyMethod.desc);
	}
	
	protected static String getDesc(int opcode, String desc) {
		if (opcode != INVOKESTATIC && opcode != NEW) {
			Type args[] = Type.getArgumentTypes(desc);
			return Type.getMethodDescriptor(Type.getReturnType(desc), ArrayUtils.subarray(args, 1, args.length));
		}
		return desc;
	}
	
}
