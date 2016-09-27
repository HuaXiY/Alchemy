package index.alchemy.core.asm.transformer;

import java.io .IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

import static index.alchemy.core.AlchemyConstants.*;

public class AlchemyTransformerManager implements IClassTransformer {
	
	public static final String ALCHEMY_HOOKS_CLASS = "index.alchemy.core.AlchemyHooks", ALCHEMY_HOOKS_DESC = "index/alchemy/core/AlchemyHooks",
			ANNOTATION_DESC = Type.getDescriptor(Hook.class), HOOK_RESULT_DESC = "index/alchemy/api/annotation/Hook$Result";
	
	public static final Logger logger = LogManager.getLogger(AlchemyTransformerManager.class.getSimpleName());
	
	public static final Map<String, IClassTransformer> transformer_mapping = new HashMap<String, IClassTransformer>();
	static {
		try {
			loadAllHook();
			loadAllTransform();
		} catch (IOException e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	@Unsafe(Unsafe.ASM_API)
	public static void loadAllHook() throws IOException {
		ClassReader reader = new ClassReader(ALCHEMY_HOOKS_CLASS);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode methodNode : node.methods)
			if (methodNode.visibleAnnotations != null)
				for (AnnotationNode annotationNode : methodNode.visibleAnnotations)
					if (annotationNode.desc.equals(ANNOTATION_DESC)) {
						Hook hook = Tool.makeAnnotation(Hook.class, annotationNode.values, "isStatic", false);
						String args[] = hook.value().split("#");
						if (args.length == 2) {
							transformer_mapping.put(args[0], new TransformerHook(methodNode, args[0], args[1], hook.isStatic()));
						} else
							AlchemyRuntimeException.onException(new RuntimeException("@Hook method -> split(\"#\") != 2"));
					}
	}
	
	@Unsafe(Unsafe.ASM_API)
	public static void loadAllTransform() throws IOException {
		ClassPath path = ClassPath.from(AlchemyTransformerManager.class.getClassLoader());
		for (ClassInfo info : path.getTopLevelClassesRecursive(MOD_TRANSFORMER_PACKAGE)) {
			Class clazz = info.load();
			if (Tool.isInstance(IAlchemyClassTransformer.class, clazz))
				try {
					IAlchemyClassTransformer transformer = (IAlchemyClassTransformer) clazz.newInstance();
					transformer_mapping.put(transformer.getTransformerClassName(), transformer);
				} catch (Exception e) { }
		}
	}

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		IClassTransformer transformer = transformer_mapping.get(transformedName);
		if (transformer != null)
			return transformer.transform(name, transformedName, basicClass);
		return basicClass;
	}
	
	public static void transform(String name) {
		logger.info("Transform: " + name);
	}

}
