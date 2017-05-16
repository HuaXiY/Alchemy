package index.alchemy.dlcs.ldc_lang.core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import static org.objectweb.asm.Opcodes.*;

@Omega
public class TransformerLdcString implements IAlchemyClassTransformer {
	
	public static final Map<String, String> mapping = Maps.newHashMap();
	public static final Set<String> mapping_classes = Sets.newHashSet();
	
	public static void loadLangMapping(File file) throws IOException {
		String str = Tool.read(file);
		for (String line : str.split("\n")) {
			if (line.isEmpty() || line.charAt(0) == '-')
				continue;
			int index = line.indexOf('=');
			if (index != -1 && line.length() >= index + 1) {
				String key = line.substring(0, index);
				int clazzIndex = key.indexOf('#');
				if (clazzIndex != -1)
					mapping_classes.add(key.substring(0, clazzIndex));
				mapping.put(key, line.substring(index + 1).replace("\\n", "\n"));
			}
		}
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!mapping_classes.contains(transformedName))
			return basicClass;
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode method : node.methods) {
			int index = 0;
			for (Iterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
				AbstractInsnNode insn = iterator.next();
				if (insn instanceof LdcInsnNode) {
					LdcInsnNode ldc = (LdcInsnNode) insn;
					if (ldc.cst instanceof String)
						ldc.cst = map(transformedName, method.name, index++, (String) ldc.cst);
				}
			}
		}
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public String map(String clazz, String method, int index, String cst) {
		String result = mapping.get(clazz + "#" + method + "@" + index);
		return result != null ? result : cst;
	}
	
	@Override
	public String getTransformerClassName() { return null; }

}
