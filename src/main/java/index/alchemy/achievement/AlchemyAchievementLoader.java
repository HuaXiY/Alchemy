package index.alchemy.achievement;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.common.LoaderState.ModState;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import index.alchemy.item.AlchemyItemLoader;

@Init(state = ModState.POSTINITIALIZED)
public class AlchemyAchievementLoader {
	
	public static AchievementPage alchemy = new AchievementPage(Constants.MODID);
	
	public static Achievement 
			use_scroll = new AchievementUseItem("use_scroll", 1, 2, AlchemyItemLoader.scroll_lightning, null);
	
	public static void init() {
		AchievementPage.registerAchievementPage(alchemy);
	}

}