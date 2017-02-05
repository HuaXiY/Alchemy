package index.alchemy.dlcs.exnails.core;

import java.io.File;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Premise;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.EventHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import toughasnails.handler.ExtendedStatHandler;
import toughasnails.handler.thirst.VanillaDrinkHandler;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.modifier.TemperatureModifier;

import static index.alchemy.dlcs.exnails.core.ExNails.*;

@Alpha
@Hook.Provider
@Field.Provider
@Premise("ToughAsNails")
@Init(state = ModState.POSTINITIALIZED)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class ExNails {
	
	public static final String
			DLC_ID = "exnails",
			DLC_NAME = "ExNails",
			DLC_VERSION = "0.0.1-dev";
	
	public static final IFieldAccess<TemperatureModifier, TemperatureDebugger> debugger = null;
	
	public static final File
			ITEM_THIRST_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/item_thirst.cfg"),
			ITEM_POTION_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/item_potion.cfg");
	
	public static final CommandReloadThirst reload_thirst = new CommandReloadThirst();
	public static final CommandReloadPotion reload_potion = new CommandReloadPotion();
	
	@Hook("toughasnails.handler.ExtendedStatHandler#onPlayerTick")
	public static Hook.Result onPlayerTick(ExtendedStatHandler handler, PlayerTickEvent event) {
		return event.player.capabilities.isCreativeMode ? Hook.Result.NULL : Hook.Result.VOID;
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		try {
			if (!ITEM_THIRST_CFG.exists())
				Tool.save(ITEM_THIRST_CFG, Tool.read(ExNails.class.getResourceAsStream("/item_thirst.cfg")));
			ExThirstLoader.loadConfig(ITEM_THIRST_CFG);
			if (!ITEM_POTION_CFG.exists())
				Tool.save(ITEM_POTION_CFG, Tool.read(ExNails.class.getResourceAsStream("/item_potion.cfg")));
			ExPotionLoader.loadConfig(ITEM_POTION_CFG);
		} catch (Exception e) { e.printStackTrace(); }
		EventHelper.unregister(MinecraftForge.EVENT_BUS, VanillaDrinkHandler.class);
	}
	
}
