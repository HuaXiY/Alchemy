package index.alchemy.achievement;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.LoaderState.ModState;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.item.ItemScroll;

@Init(state = ModState.POSTINITIALIZED)
public class AlchemyAchievementLoader {
	
	public static final AchievementPage alchemy = new AchievementPage(AlchemyConstants.MOD_ID);
	
	public static final Achievement 
			use_scroll = new AchievementUseItem("use_scroll", 1, 2, AlchemyItemLoader.scroll_lightning, ItemScroll.class, null);
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		AchievementPage.registerAchievementPage(alchemy);
	}

}