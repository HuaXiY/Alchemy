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
		LaunchClassLoader loader = AlchemyCorePlugin.getLaunchClassLoader();
		loader.addTransformerExclusion("javafx.");
		TransformerSide.tryInject(loader);
		if (!AlchemyCorePlugin.isRuntimeDeobfuscationEnabled())
			if (AlchemyCorePlugin.runtimeSide().isClient())
				TransformerInjectOptifine.tryInject(loader);
		return Tool.VOID;
	}

	@Override
	public void injectData(Map<String, Object> data) { }
	
	public static void checkInvokePermissions() {
		Tool.checkInvokePermissions(3, AlchemySetup.class);
	}

}
