package index.alchemy.achievement;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.LoaderState.ModState;
import index.alchemy.annotation.Init;
import index.alchemy.core.Constants;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.item.ItemScroll;

@Init(state = ModState.POSTINITIALIZED)
public class AlchemyAchievementLoader {
	
	public static final AchievementPage alchemy = new AchievementPage(Constants.MOD_ID);
	
	public static final Achievement 
			use_scroll = new AchievementUseItem("use_scroll", 1, 2, AlchemyItemLoader.scroll_lightning, ItemScroll.class, null);
	
	public static void init() {
		AchievementPage.registerAchievementPage(alchemy);
	}

}