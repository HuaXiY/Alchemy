package index.alchemy.core;

import java.util.Map;

import index.alchemy.core.asm.transformer.TransformerInjectOptifine;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
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
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			TransformerInjectOptifine.tryInject((ClassLoader) data.get("classLoader"));
	}

}
