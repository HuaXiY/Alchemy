package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyCorePlugin;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import static org.objectweb.asm.Opcodes.*;

public class TransformerDeobfuscating implements IClassTransformer {
	
	public final Map<String,Map<String,String>>
			fieldNameMaps =
				Tool.<Map<String,Map<String,String>>>$(FMLDeobfuscatingRemapper.INSTANCE, "fieldNameMaps"),
			methodNameMaps =
				Tool.<Map<String,Map<String,String>>>$(FMLDeobfuscatingRemapper.INSTANCE, "methodNameMaps");

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			return basicClass;
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (FieldNode field : node.fields)
			if (field.name.startsWith("field_"))
				field.name = map(field.name, fieldNameMaps);
		for (MethodNode method : node.methods) {
			if (method.name.startsWith("func_"))
				method.name = map(method.name, methodNameMaps);
			for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
				AbstractInsnNode insn = iterator.next();
				if (insn instanceof FieldInsnNode) {
					FieldInsnNode field = (FieldInsnNode) insn;
					if (field.name.startsWith("field_"))
						field.name = map(field.name, fieldNameMaps);
				} else if (insn instanceof InvokeDynamicInsnNode) {
					InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) insn;
					if (dynamic.name.startsWith("func_"))
						dynamic.name = map(dynamic.name, methodNameMaps);
				}
			}
		}
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public String map(String name, Map<String,Map<String,String>> maps) {
		for (Map<String, String> mapping : maps.values())
			for (Entry<String, String> entry : mapping.entrySet())
				if (entry.getKey().startsWith(name + ":") || entry.getKey().startsWith(name + "("))
					return entry.getValue();
		return name;
	}

}
