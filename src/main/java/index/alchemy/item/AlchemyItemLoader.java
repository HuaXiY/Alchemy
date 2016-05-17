package index.alchemy.item;

import index.alchemy.annotation.Init;
import net.minecraft.init.Items;
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
	
	public static final AlchemyItemBauble
			ring_space = new ItemRingSpace(),
			ring_time = new ItemRingTime(),
			ring_alive = new ItemRingAlive(),
			ring_blessing = new ItemRingBlessing(),
			belt_guard = new ItemBeltGuard(),
			belt_tough = new ItemBeltTough(),
			amulet_heal = new ItemAmuletHeal(),
			amulet_purify = new ItemAmuletPurify();
	
	public static void init() {}
	
}
