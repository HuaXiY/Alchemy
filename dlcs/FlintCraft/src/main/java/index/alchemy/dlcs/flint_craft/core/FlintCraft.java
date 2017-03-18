package index.alchemy.dlcs.flint_craft.core;

import java.util.Random;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Listener;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Always;
import index.alchemy.util.CraftingHelper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import static index.alchemy.dlcs.flint_craft.core.FlintCraft.*;

@Listener
@Init(state = ModState.PREINITIALIZED)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class FlintCraft {
	
	public static final String
			DLC_ID = "flint_craft",
			DLC_NAME = "FlintCraft",
			DLC_VERSION = "0.0.1-dev";
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		replaceItem();
		CraftingHelper.add(Items.WOODEN_AXE,		"bb ", "ba ", " a ", 'a', Items.STICK, 'b', Items.FLINT);
		CraftingHelper.add(Items.WOODEN_HOE,		"bb ", " a ", " a ", 'a', Items.STICK, 'b', Items.FLINT);
		CraftingHelper.add(Items.WOODEN_PICKAXE,	"bbb", " a ", " a ", 'a', Items.STICK, 'b', Items.FLINT);
		CraftingHelper.add(Items.WOODEN_SHOVEL,		" b ", " a ", " a ", 'a', Items.STICK, 'b', Items.FLINT);
		CraftingHelper.add(Items.WOODEN_SWORD,		" b ", " b ", " a ", 'a', Items.STICK, 'b', Items.FLINT);
	}
	
	private static void replaceItem() {
		replaceItem(Items.WOODEN_AXE,		"flint_axe");
		replaceItem(Items.WOODEN_HOE,		"flint_hoe");
		replaceItem(Items.WOODEN_PICKAXE,	"flint_pickaxe");
		replaceItem(Items.WOODEN_SHOVEL,	"flint_shovel");
		replaceItem(Items.WOODEN_SWORD,		"flint_sword");
	}
	
	private static void replaceItem(Item item, String location) {
		if (Always.isClient())
			replaceTextures(item, location);
		CraftingHelper.remove(item);
	}
	
	@SideOnly(Side.CLIENT)
	private static void replaceTextures(Item item, String location) {
		ModelLoader.setCustomModelResourceLocation(item, 0,
				new ModelResourceLocation(new ResourceLocation("flint_craft", location), "inventory"));
	}
	
	private static final Random random = new Random();
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onBlock_HarvestDrops(BlockEvent.HarvestDropsEvent event) {
		Item block = Item.getItemFromBlock(event.getState().getBlock());
		for (ItemStack item : OreDictionary.getOres("logWood"))
			if (item.getItem() == block) {
				if (event.getHarvester() != null) {
					ItemStack hand = event.getHarvester().getHeldItemMainhand();
					if (hand == null || !(hand.getItem() instanceof ItemAxe) || !hand.getItem().getToolClasses(hand).contains("axe"))
						event.getDrops().clear();
				}
				break;
			}
		for (ItemStack item : OreDictionary.getOres("treeLeaves"))
			if (item.getItem() == block) {
				int r = random.nextInt(16);
				if (r < 4)
					event.getDrops().add(new ItemStack(Items.STICK, r == 0 ? 2 : 1));
				break;
			}
	}
	
}
