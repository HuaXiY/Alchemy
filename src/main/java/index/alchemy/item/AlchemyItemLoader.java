package index.alchemy.item;

import index.alchemy.core.Init;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.util.Tool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyItemLoader {
	
	public static final ItemMagicSolvent 
			solvent_lapis_lazuli = new ItemMagicSolvent("lapis_lazuli", 0x307CCB, Items.DYE, 4);
	
	public static final ItemScroll 
			scroll_boom = new ItemScrollBOOM(),
			scroll_ice_screen = new ItemScrollIceScreen(),
			scroll_lightning = new ItemScrollLightning(),
			scroll_tp = new ItemScrollTP();
	
	public static final ItemRingSpace ring_space = new ItemRingSpace();
	public static final ItemAmuletGuard amulet_guard = new ItemAmuletGuard();
	
	public static void init() {
		
	}
	
}
