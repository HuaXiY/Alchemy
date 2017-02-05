package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.google.common.base.Joiner;
import com.google.common.io.CharSource;

import index.alchemy.core.asm.transformer.AlchemyTransformerManager;
import index.alchemy.core.asm.transformer.TransformerInjectOptifine;
import index.alchemy.core.asm.transformer.TransformerSide;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

import static index.alchemy.util.Tool.$;

@Beta
public class AlchemySetup implements IFMLCallHook {
	
	@Override
	public Void call() throws Exception {
		AlchemyModLoader.logger.info("Setup: " + AlchemySetup.class.getName());
		LaunchClassLoader loader = AlchemyEngine.getLaunchClassLoader();
		injectAccessTransformer(AlchemyModLoader.mod_path, loader);
		loader.addTransformerExclusion("javafx.");
		TransformerSide.inject(loader);
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled())
			if (AlchemyEngine.runtimeSide().isClient())
				TransformerInjectOptifine.tryInject(loader);
		List<IClassTransformer> transformers = $(loader, "transformers");
		AlchemyTransformerManager.logger.info(Joiner.on('\n').appendTo(new StringBuilder("Transformers: \n"), transformers).toString());
		return Tool.VOID;
	}

	@Override
	public void injectData(Map<String, Object> data) { }
	
	public static void injectAccessTransformer(File file, LaunchClassLoader loader) throws IOException {
		injectAccessTransformer(file, "forge.at", loader);
	}
	
	public static void injectAccessTransformer(File file, String atName, LaunchClassLoader loader) throws IOException {
		if (!AlchemyEngine.isRuntimeDeobfuscationEnabled())
			return;
		String at = null;
		try (JarFile jar = new JarFile(file)) {
			ZipEntry entry = jar.getEntry("META-INF/" + atName);
	        if (entry != null)
	        	at = Tool.read(jar.getInputStream(entry));
		}
		if (at != null) {
			List<IClassTransformer> transformers = $(loader, "transformers");
			for (IClassTransformer t : transformers)
				if (t instanceof AccessTransformer) {
					AccessTransformer transformer = (AccessTransformer) t;
					$(transformer, "processATFile", CharSource.wrap(at));
					break;
				}
		}
    }

}
