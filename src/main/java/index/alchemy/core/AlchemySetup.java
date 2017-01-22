package index.alchemy.core;

import java.util.Map;

import index.alchemy.core.asm.transformer.TransformerInjectOptifine;
import index.alchemy.core.asm.transformer.TransformerSide;
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
		TransformerSide.tryInject(loader);
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			if (AlchemyCorePlugin.runtimeSide().isClient())
				TransformerInjectOptifine.tryInject(loader);
	}

}
