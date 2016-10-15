package index.alchemy.core.asm.transformer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.TypeInsnNode;

import index.alchemy.api.annotation.SideOnlyLambda;
import index.alchemy.util.ASMHelper;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.objectweb.asm.Opcodes.*;
import static index.alchemy.core.AlchemyConstants.*;

public class TransformerSideLambda implements IClassTransformer {
	
	public static final Side runtime_side = FMLLaunchHandler.side();

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!transformedName.startsWith(MOD_PACKAGE))
			return basicClass;
		String 	side_desc = ASMHelper.getClassDesc("net.minecraftforge.fml.relauncher.SideOnly"),
				side_lambda_desc = ASMHelper.getClassDesc("index.alchemy.api.annotation.SideOnlyLambda");
		LinkedList<Type> types = new LinkedList<>();
		LinkedList<Boolean> marks = new LinkedList<>();
		int flag = -1;
		ClassReader reader;
		try {
			reader = new ClassReader(transformedName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ClassWriter writer = new ClassWriter(0);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (Iterator<MethodNode> iterator = node.methods.iterator(); iterator.hasNext();) {
			MethodNode method = iterator.next();
			Side side = null;
			if (method.visibleAnnotations != null)
				for (AnnotationNode ann : method.visibleAnnotations)
					if (ann.desc.equals(side_desc))
						side = Tool.makeAnnotation(SideOnly.class, ann.values).value();
			for (Iterator<AbstractInsnNode> insnIterator = method.instructions.iterator(); insnIterator.hasNext(); flag--) {
				AbstractInsnNode insn = insnIterator.next();
				if (insn instanceof InvokeDynamicInsnNode) {
					InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) insn;
					Type type = Type.getReturnType(dynamic.desc);
					types.add(type);
					marks.add(side != null && side != runtime_side);
					flag = 3;
				}
				if (flag == 0 && !marks.getLast() && insn instanceof TypeInsnNode) {
					TypeInsnNode type = (TypeInsnNode) insn;
					if (Type.getType(ASMHelper.getClassDesc(type.desc)).equals(types.getLast()) &&
							insn.visibleTypeAnnotations != null)
						for (TypeAnnotationNode ann : insn.visibleTypeAnnotations)
							if (ann.desc.equals(side_lambda_desc) &&
									Tool.makeAnnotation(SideOnlyLambda.class, ann.values).value() != runtime_side)
								marks.set(marks.size() - 1, true);
				}
			}
		}
		reader = new ClassReader(basicClass);
		node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (Iterator<MethodNode> iterator = node.methods.iterator(); iterator.hasNext();) {
			MethodNode method = iterator.next();
			if (!marks.isEmpty() && method.name.startsWith("lambda$") && (method.access & ACC_SYNTHETIC) != 0) {
				if (marks.getFirst())
					iterator.remove();
				types.removeFirst();
				marks.removeFirst();
			}
		}
		node.accept(writer);
		return writer.toByteArray();
	}

}
