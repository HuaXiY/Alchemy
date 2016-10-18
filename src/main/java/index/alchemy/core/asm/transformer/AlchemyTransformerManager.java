package index.alchemy.core.asm.transformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Proxy;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

import static index.alchemy.core.AlchemyConstants.*;

public class AlchemyTransformerManager implements IClassTransformer {
	
	public static final String 
			ALCHEMY_HOOKS_CLASS = "index.alchemy.core.AlchemyHooks",
			ALCHEMY_HOOKS_DESC = "index/alchemy/core/AlchemyHooks",
			HOOK_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Hook;",
			HOOK_RESULT_DESC = "index/alchemy/api/annotation/Hook$Result",
			PROXY_ANNOTATION_DESC = "Lindex/alchemy/api/annotation/Proxy;";
	
	public static final Logger logger = LogManager.getLogger(AlchemyTransformerManager.class.getSimpleName());
	
	public static final Map<String, List<IClassTransformer>> transformers_mapping = new HashMap<String, List<IClassTransformer>>() {
		@Override
		public List<IClassTransformer> get(Object key) {
			List<IClassTransformer> result = super.get(key);
			if (result == null)
				put((String) key, result = new LinkedList());
			return result;
		}
	};
	static {
		try {
			loadAllHook();
			loadAllProxy();
			loadAllTransform();
		} catch (IOException e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	public static final boolean debug_print = Boolean.getBoolean("index.alchemy.core.asm.classloader.at.debug.print");
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadAllHook() throws IOException {
		ClassReader reader = new ClassReader(ALCHEMY_HOOKS_CLASS);
		ClassNode node = new ClassNode(ASM5);
		reader.accept(node, 0);
		for (MethodNode methodNode : node.methods)
			if (methodNode.visibleAnnotations != null)
				for (AnnotationNode ann : methodNode.visibleAnnotations)
					if (ann.desc.equals(HOOK_ANNOTATION_DESC)) {
						Hook hook = Tool.makeAnnotation(Hook.class, ann.values,
								"isStatic", false, "type", Hook.Type.HEAD, "disable", "");
						if (hook.disable().isEmpty() || !Boolean.getBoolean(hook.disable())) {
							String args[] = hook.value().split("#");
							if (args.length == 2)
								transformers_mapping.get(args[0]).add(new TransformerHook(methodNode, args[0], args[1],
										hook.isStatic(), hook.type()));
							else
								AlchemyRuntimeException.onException(new RuntimeException("@Hook method -> split(\"#\") != 2"));
						}
					}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadAllProxy() throws IOException {
		ClassPath path = ClassPath.from(AlchemyTransformerManager.class.getClassLoader());
		for (ClassInfo info : path.getTopLevelClassesRecursive(MOD_PACKAGE.substring(0, MOD_PACKAGE.length() - 1))) {
			ClassReader reader = new ClassReader(info.getName());
			ClassNode node = new ClassNode(ASM5);
			reader.accept(node, 0);
			if (node.visibleAnnotations != null)
				for (AnnotationNode ann : node.visibleAnnotations)
					if (ann.desc.equals(PROXY_ANNOTATION_DESC)) {
						Proxy proxy = Tool.makeAnnotation(Proxy.class, ann.values);
						transformers_mapping.get(proxy.value()).add(new TransformerProxy(node));
					}
		}
	}
	
	@Unsafe(Unsafe.ASM_API)
	private static void loadAllTransform() throws IOException {
		ClassPath path = ClassPath.from(AlchemyTransformerManager.class.getClassLoader());
		for (ClassInfo info : path.getTopLevelClassesRecursive(MOD_TRANSFORMER_PACKAGE.substring(0, MOD_TRANSFORMER_PACKAGE.length() - 1))) {
			Class clazz = info.load();
			if (Tool.isInstance(IAlchemyClassTransformer.class, clazz))
				try {
					IAlchemyClassTransformer transformer = (IAlchemyClassTransformer) clazz.newInstance();
					transformers_mapping.get(transformer.getTransformerClassName()).add(transformer);
				} catch (Exception e) { }
		}
	}

	@Override
	@Unsafe(Unsafe.ASM_API)
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (debug_print)
			if (transformedName.startsWith("index."))
				logger.info("loading: " + transformedName);
		if (transformers_mapping.containsKey(transformedName)) {
			List<IClassTransformer> transformers = transformers_mapping.get(transformedName);
			for (IClassTransformer transformer : transformers)
				basicClass = transformer.transform(name, transformedName, basicClass);
		}
		return basicClass;
	}
	
	public static void loadAllTransformClass() {
		for (String clazz : transformers_mapping.keySet())
			Tool.forName(clazz, false);
	}
	
	public static void transform(String name) {
		logger.info("Transform: " + name);
	}

}
