package index.alchemy.item;

import index.alchemy.core.Init;
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
	
	public static final ItemRingSpace ring_space = new ItemRingSpace();
	public static final ItemRingTime rint_time = new ItemRingTime();
	public static final ItemBeltGuard amulet_guard = new ItemBeltGuard();
	public static final ItemAmuletHeal amulet_heal = new ItemAmuletHeal();
	
	public static void init() {}
	
}
