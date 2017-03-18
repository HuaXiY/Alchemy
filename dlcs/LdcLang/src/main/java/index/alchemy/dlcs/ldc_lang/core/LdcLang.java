package index.alchemy.dlcs.ldc_lang.core;

import java.io.File;
import java.io.IOException;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState.ModState;

import static index.alchemy.dlcs.ldc_lang.core.LdcLang.*;

@Init(state = ModState.INITIALIZED)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class LdcLang {
	
	public static final String
			DLC_ID = "ldc_lang",
			DLC_NAME = "LdcLang",
			DLC_VERSION = "0.0.1-dev";
	
	public static void init() {
		if (FMLCommonHandler.instance().getSide().isClient())
			ClientCommandHandler.instance.registerCommand(new CommandMakeLang());
	}
	
	static { loadLangMapping(); }

	public static void loadLangMapping() {
		File dir = new File(AlchemyModLoader.mc_dir, "lang");
		if (!dir.exists())
			dir.mkdirs();
		for (File file : dir.listFiles())
			if (!file.isDirectory())
				try {
					TransformerLdcString.loadLangMapping(file);
				} catch (IOException e) { e.printStackTrace(); }
	}

}
