package index.alchemy.core;

import java.io.File;
import java.net.URL;
import java.util.Map;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
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
	
	static {
		String libs = System.getProperty("index.alchemy.runtime.lib.ext");
		if (libs != null)
			for (String lib : libs.split(";"))
				addRuntimeExtLibFromJRE(lib);
	}
	
	public static void addRuntimeExtLibFromJRE(String name) {
		try {
			URL url = new File(System.getProperty("java.home") + "/lib/ext/" + name + ".jar").toURI().toURL();
			Tool.addURLToClassLoader(AlchemyCorePlugin.class.getClassLoader(), url);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	private static boolean runtimeDeobfuscationEnabled;
	
	public static boolean isRuntimeDeobfuscationEnabled() {
		return runtimeDeobfuscationEnabled;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"index.alchemy.core.asm.transformer.TransformerSideLambda",
				"index.alchemy.core.asm.transformer.TransformerDeobfuscating"
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
