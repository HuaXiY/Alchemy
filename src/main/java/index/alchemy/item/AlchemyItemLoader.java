package index.alchemy.item;

import index.alchemy.api.annotation.Init;
import index.alchemy.item.ItemMagicSolvent.Type;
import index.alchemy.sound.AlchemySoundLoader;
import index.project.version.annotation.Omega;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED)
public class AlchemyItemLoader {
	
	//public static final AlchemyItem
	
	public static final ItemMagicDust
			dush_witchcraft = new ItemMagicDust("dush_witchcraft", 0x996633, null);
	
	public static final ItemIngot
			ingot_silver = new ItemIngot("ingot_silver", 0xFFFFFF);
	
	public static final ItemMagicSolvent 
			solvent_lapis_lazuli = new ItemMagicSolvent("lapis_lazuli", 0x307CCB, Type.UNSTABLE, Items.DYE, 4);
	
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
	
	public static final AlchemyItemRecord
			record_re_awake = new AlchemyItemRecord("re_awake", AlchemySoundLoader.record_re_awake, 0xFF3333);
	
}