package index.alchemy.interacting;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import index.alchemy.api.Alway;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;

@Init(state = ModState.CONSTRUCTED)
public class Premise {
	
	public static final List<String> MODID_LIST = new LinkedList<String>();
	static {
		MODID_LIST.add("BiomesOPlenty");
		MODID_LIST.add("Baubles");
	}
	
	public static void init() {
		for (String modid : MODID_LIST)
			if (!Loader.isModLoaded(modid))
				onMiss(modid);
	}
	
	public static void onMiss(String modid) {
		if (Alway.isClient()) {
			SplashProgress.finish();
			Display.destroy();
			JDialog dialog = new JDialog();
			dialog.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(dialog, "Could not find a prerequisite mod: " + modid,
					"Minecraft-" + Constants.MOD_ID, JOptionPane.ERROR_MESSAGE);
		}
		AlchemyModLoader.logger.error("Could not find a prerequisite mod: " + modid);
		System.setProperty("fml.debugExit", "true");
		FMLCommonHandler.instance().exitJava(-0xC001, false);
	}

}
