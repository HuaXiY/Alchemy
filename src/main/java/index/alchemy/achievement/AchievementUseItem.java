package index.alchemy.achievement;

import index.alchemy.api.IEventHandle;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class AchievementUseItem extends AlchemyAchievement implements IEventHandle {
	
	private Class<? extends Item> item;
	
	public AchievementUseItem(String name, int column, int row, Item icon, Achievement parent) {
		this(name, column, row, icon, icon.getClass(), parent);
	}
	
	public AchievementUseItem(String name, int column, int row, Item icon, Class<? extends Item> clazz, Achievement parent) {
		super(name, column, row, icon, parent);
		item = clazz;
	}

	@SubscribeEvent
	public void onLivingEntityUseItem_Finish(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntityLiving() instanceof EntityPlayer && item.isInstance(event.getItem().getItem()))
			((EntityPlayer) event.getEntityLiving()).addStat(this);
	}

}