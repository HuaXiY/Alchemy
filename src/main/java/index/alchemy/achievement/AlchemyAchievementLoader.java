//package index.alchemy.achievement;
//
//import net.minecraft.init.Blocks;
//import net.minecraft.init.Items;
//import net.minecraft.item.Item;
//import net.minecraft.stats.Achievement;
//import net.minecraftforge.common.AchievementPage;
//import net.minecraftforge.fml.common.LoaderState.ModState;
//import index.alchemy.api.annotation.Init;
//import index.alchemy.core.AlchemyModLoader;
//import index.alchemy.core.AlchemyConstants;
//import index.alchemy.item.AlchemyItemLoader;
//import index.project.version.annotation.Omega;
//
//@Omega
//@Init(state = ModState.POSTINITIALIZED)
//public class AlchemyAchievementLoader {
//	
//	public static final AchievementPage alchemy = new AchievementPage(AlchemyConstants.MOD_ID);
//	
//	public static final Achievement
//			delicious_dirt = new AlchemyAchievement("delicious_dirt", -8, -6, Item.getItemFromBlock(Blocks.DIRT), null),
//			tasty_lava = new AlchemyAchievement("tasty_lava", -6, -6, Items.LAVA_BUCKET, null),
//			use_scroll = new AlchemyAchievement("use_scroll", 1, 2, AlchemyItemLoader.scroll_lightning, null);
//	
//	public static void init() {
//		AlchemyModLoader.checkInvokePermissions();
//		AlchemyModLoader.checkState();
//		AchievementPage.registerAchievementPage(alchemy);
//	}
//
//}