package index.alchemy.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import static index.alchemy.core.AlchemyConstants.*;

@Name(MOD_ID)
@MCVersion(MC_VERSION)
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions(MOD_TRANSFORMER_PACKAGE)
public class AlchemyCorePlugin implements IFMLLoadingPlugin {
	
	private static boolean runtimeDeobfuscationEnabled;
	
	public static boolean isRuntimeDeobfuscationEnabled() {
		return runtimeDeobfuscationEnabled;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				
		};
	}

	@Override
	public String getModContainerClass() {
		return "index.alchemy.core.AlchemyModContainer";
	}

	@Override
	public String getSetupClass() {
		return getModContainerClass();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		runtimeDeobfuscationEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return "index.alchemy.core.asm.transformer.AlchemyTransformerManager";
	}

}
