package index.alchemy.dlcs.ldc_lang.core;

import java.io.File;
import java.io.IOException;

import index.alchemy.api.annotation.DLC;
import index.alchemy.core.AlchemyModLoader;
import index.project.version.annotation.Omega;
import net.minecraft.command.CommandBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.dlcs.ldc_lang.core.LdcLang.*;

@Omega
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class LdcLang {
	
	public static final String
			DLC_ID = "ldc_lang",
			DLC_NAME = "LdcLang",
			DLC_VERSION = "0.0.1-dev";
	
	@SideOnly(Side.CLIENT)
	public static final CommandBase make_lang = new CommandMakeLang();
	
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
