package index.alchemy.core.asm.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;

import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.common.asm.transformers.SideTransformer;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.$;
import static org.objectweb.asm.Opcodes.*;

public class TransformerSide extends SideTransformer {
	
	public final IClassTransformer parent;
	
	public static void inject(LaunchClassLoader classLoader) {
		try {
			List<IClassTransformer> transformers = $(classLoader, "transformers");
			transformers.stream().filter(TransformerWrapper.class::isInstance).forEach(t -> {
				IClassTransformer parent = $(t, "parent");
				if (parent instanceof SideTransformer)
					$(t, "parent<", new TransformerSide(parent));
			});
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.startsWith(MOD_PACKAGE))
			return parent.transform(name, transformedName, basicClass);
		
		if (basicClass == null)
			return null;

		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(node, 0);

		if (check(node.visibleAnnotations))
			throw new RuntimeException(String.format("Attempted to load class %s for invalid side %s", node.name,
					AlchemyEngine.runtimeSide()));
		
		List<FieldNode> invalidField = Lists.newLinkedList(), invalidStaticField = Lists.newLinkedList();

		for (Iterator<FieldNode> iterator = node.fields.iterator(); iterator.hasNext();) {
			FieldNode field = iterator.next();
			if (check(field.visibleAnnotations)) {
				iterator.remove();
				((field.access & ACC_STATIC) == 0 ? invalidField : invalidStaticField).add(field);
			}
		}
		
		for (Iterator<MethodNode> iterator = node.methods.iterator(); iterator.hasNext();) {
			MethodNode method = iterator.next();
			if (check(method.visibleAnnotations)) {
				iterator.remove();
				continue;
			}
			Boolean flag = method.name.equals("<init>") ? Boolean.TRUE : method.name.equals("<clinit>") ? Boolean.FALSE : null;
			if (flag != null) {
				List<FieldNode> list = flag ? invalidField : invalidStaticField;
				int opcode = flag ? PUTFIELD : PUTSTATIC;
				for (ListIterator<AbstractInsnNode> insnIterator = method.instructions.iterator(); insnIterator.hasNext();) {
					AbstractInsnNode insn = insnIterator.next();
					if (insn instanceof FieldInsnNode) {
						FieldInsnNode fieldInsn = (FieldInsnNode) insn;
						for (FieldNode field : list)
							if (ASMHelper.corresponding(field, node.name, fieldInsn)) {
								if (fieldInsn.getOpcode() == opcode)
									ASMHelper.removeInvoke(insnIterator);
								break;
							}
					}
				}
			}
		}

		ClassWriter writer = ASMHelper.newClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	protected boolean check(List<AnnotationNode> anns) {
		if (anns == null)
			return false;
		for (AnnotationNode ann : anns)
			if (ann.desc.equals(AlchemyTransformerManager.SIDE_ONLY_ANNOTATION_DESC))
					if (Tool.makeAnnotation(SideOnly.class, ann.values).value() != AlchemyEngine.runtimeSide())
						return true;
		return false;
	}
	
	public TransformerSide(@Nonnull IClassTransformer parent) {
		this.parent = parent;
	}

}
