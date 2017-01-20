package index.alchemy.core;

import java.util.Map;

import index.alchemy.core.asm.transformer.TransformerInjectOptifine;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

@Beta
public class AlchemySetup implements IFMLCallHook {

	@Override
	public Void call() throws Exception {
		AlchemyDLCLoader.setup();
		return Tool.VOID;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		LaunchClassLoader loader = (LaunchClassLoader) data.get("classLoader");
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			TransformerInjectOptifine.tryInject(loader);
	}

}
