package index.alchemy.achievement;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.IEventHandle;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

public class AlchemyAchievement extends Achievement {
	
	public AlchemyAchievement(String name, int column, int row, Item icon, Achievement parent) {
		super("achievement." + name, name, column, row, icon, parent);
		init(parent);
	}

	public AlchemyAchievement(String name, int column, int row, ItemStack icon, Achievement parent) {
		super("achievement." + name, name, column, row, icon, parent);
		init(parent);
	}
	
	public AlchemyAchievement(String name, int column, int row, Block icon, Achievement parent) {
		super("achievement." + name, name, column, row, icon, parent);
		init(parent);
	}
	
	private void init(Achievement parent) {
		AlchemyAchievementLoader.alchemy.getAchievements().add(this);
		
		if (parent == null)
			initIndependentStat().registerStat();
		else 
			setSpecial().registerStat();
		
		if (this instanceof IEventHandle)
			AlchemyEventSystem.registerEventHandle((IEventHandle) this);
	}
	
}