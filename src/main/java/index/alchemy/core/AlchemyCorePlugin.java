package index.alchemy.core;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.util.Tool.$;

@Omega
@Name(MOD_ID)
@MCVersion(MC_VERSION)
@SortingIndex(Integer.MAX_VALUE)
@TransformerExclusions(MOD_TRANSFORMER_PACKAGE)
public class AlchemyCorePlugin implements IFMLLoadingPlugin {
	
	static {
		String libs = System.getProperty("index.alchemy.runtime.lib.ext");
		if (libs != null) {
			Set<String> libSet = Sets.newHashSet(Splitter.on(';').split(libs));
			libSet.add("jfxrt");
			libSet.forEach(AlchemyCorePlugin::addRuntimeExtLibFromJRE);
		}
	}
	
	public static void addRuntimeExtLibFromJRE(String name) {
		try {
			URL url = new File(System.getProperty("java.home") + "/lib/ext/" + name + ".jar").toURI().toURL();
			Tool.addURLToClassLoader(getLaunchClassLoader(), url);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	private static boolean runtimeDeobfuscationEnabled = !Boolean.getBoolean("index.alchemy.runtime.deobf.disable");
	
	public static boolean isRuntimeDeobfuscationEnabled() {
		return runtimeDeobfuscationEnabled;
	}
	
	public static File getMinecraftDir() {
		return $(CoreModManager.class, "mcDir");
	}
	
	public static LaunchClassLoader getLaunchClassLoader() {
		return Launch.classLoader;
	}
	
	@Override
	public void injectData(Map<String, Object> data) { }
	
	private static final Side side = FMLLaunchHandler.side();
	
	public static Side runtimeSide() {
		return side;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				"index.alchemy.core.asm.transformer.TransformerSideLambda",
				"index.alchemy.core.asm.transformer.TransformerDeobfuscating",
				"index.alchemy.core.asm.transformer.TransformerGenericEvent"
		};
	}

	@Override
	public String getModContainerClass() {
		return "index.alchemy.core.AlchemyModContainer";
	}

	@Override
	public String getSetupClass() {
		return "index.alchemy.core.AlchemySetup";
	}

	@Override
	public String getAccessTransformerClass() {
		return "index.alchemy.core.asm.transformer.AlchemyTransformerManager";
	}

}
