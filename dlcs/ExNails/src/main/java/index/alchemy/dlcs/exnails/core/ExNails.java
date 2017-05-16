package index.alchemy.dlcs.exnails.core;

import java.io.File;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Premise;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.entity.AlchemyDamageSource;
import index.alchemy.util.EventHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import toughasnails.api.season.Season;
import toughasnails.block.BlockTANTemperatureCoil;
import toughasnails.handler.ExtendedStatHandler;
import toughasnails.handler.season.SeasonHandler;
import toughasnails.handler.thirst.VanillaDrinkHandler;
import toughasnails.potion.PotionHyperthermia;
import toughasnails.potion.PotionHypothermia;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.modifier.TemperatureModifier;

import static index.alchemy.dlcs.exnails.core.ExNails.*;

@Beta
@Listener
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
	
	public static final CommandBase
			reload_food = new ExFoodLoader.CommandReloadFood(),
			reload_thirst = new ExThirstLoader.CommandReloadThirst(),
			reload_potion = new ExPotionLoader.CommandReloadPotion(),
			reload_temperature = new ExTemperatureLoader.CommandReloadTemperature();
	
	@Hook("toughasnails.handler.ExtendedStatHandler#onPlayerTick")
	public static Hook.Result onPlayerTick(ExtendedStatHandler handler, PlayerTickEvent event) {
		return event.player.capabilities.isCreativeMode ? Hook.Result.NULL : Hook.Result.VOID;
	}
	
	public static final DamageSource temperature = new AlchemyDamageSource("temperature").setPureDamage()
			.setDamageBypassesArmor().setDamageIsAbsolute();
	
	@Hook("toughasnails.potion.PotionHyperthermia#func_76394_a")
	public static Hook.Result performEffect(PotionHyperthermia hyperthermia, EntityLivingBase living, int amplifier) {
		living.attackEntityFrom(temperature, 0.5F);
		return Hook.Result.NULL;
	}
	
	@Hook("toughasnails.potion.PotionHypothermia#func_76394_a")
	public static Hook.Result performEffect(PotionHypothermia hypothermia, EntityLivingBase living, int amplifier) {
		living.attackEntityFrom(temperature, 0.5F);
		return Hook.Result.NULL;
	}
	
	@Hook(value = "toughasnails.season.SeasonASMHelper#onUpdateTick", isStatic = true)
	public static Hook.Result onUpdateTick(BlockCrops block, World world, BlockPos pos) {
		return Hook.Result.NULL;
	}
	
	@Hook("toughasnails.block.BlockTANTemperatureCoil#func_149915_a")
	public static Hook.Result createNewTileEntity(BlockTANTemperatureCoil block, World world, int meta) {
		return Hook.Result.NULL;
	}
	
	@Hook("toughasnails.block.BlockTANTemperatureCoil#func_149915_a")
	public static Hook.Result updatePowered(BlockTANTemperatureCoil block, World world, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state.withProperty(BlockTANTemperatureCoil.POWERED,
				world.isBlockPowered(pos) || world.isBlockPowered(pos.up())));
		return Hook.Result.NULL;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlock_CropGrow_Pre(BlockEvent.CropGrowEvent.Pre event) {
		if (SeasonHandler.getServerSeasonData(event.getWorld()).getSubSeason().getSeason() == Season.WINTER)
			AlchemyEventSystem.markEventIgnore(event, Event.Result.DENY);
	}
	
	public static final File
			ITEM_FOOD_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/item_food.cfg"),
			ITEM_THIRST_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/item_thirst.cfg"),
			ITEM_POTION_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/item_potion.cfg"),
			BLOCK_TEMPERATURE_CFG = new File(AlchemyEngine.getMinecraftDir(), "config/block_temperature.cfg");
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		try {
			if (!ITEM_FOOD_CFG.exists())
				Tool.save(ITEM_FOOD_CFG, Tool.read(ExNails.class.getResourceAsStream("/item_food.cfg")));
			ExFoodLoader.loadConfig(ITEM_FOOD_CFG);
		} catch (Exception e) { e.printStackTrace(); }
		try {
			if (!ITEM_THIRST_CFG.exists())
				Tool.save(ITEM_THIRST_CFG, Tool.read(ExNails.class.getResourceAsStream("/item_thirst.cfg")));
			ExThirstLoader.loadConfig(ITEM_THIRST_CFG);
		} catch (Exception e) { e.printStackTrace(); }
		try {
			if (!ITEM_POTION_CFG.exists())
				Tool.save(ITEM_POTION_CFG, Tool.read(ExNails.class.getResourceAsStream("/item_potion.cfg")));
			ExPotionLoader.loadConfig(ITEM_POTION_CFG);
		} catch (Exception e) { e.printStackTrace(); }
		try {
			if (!BLOCK_TEMPERATURE_CFG.exists())
				Tool.save(BLOCK_TEMPERATURE_CFG, Tool.read(ExNails.class.getResourceAsStream("/block_temperature.cfg")));
			ExTemperatureLoader.loadConfig(BLOCK_TEMPERATURE_CFG);
		} catch (Exception e) { e.printStackTrace(); }
		EventHelper.unregister(MinecraftForge.EVENT_BUS, VanillaDrinkHandler.class);
	}
	
}
