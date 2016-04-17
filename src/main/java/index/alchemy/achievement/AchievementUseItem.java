package index.alchemy.achievement;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AchievementUseItem extends AlchemyAchievement implements IEventHandle {
	
	private Item item;
	
	public AchievementUseItem(String name, int column, int row, Item icon, Achievement parent) {
		super(name, column, row, icon, parent);
		item = icon;
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent
	public void onLivingEntityUseItemEventFinish(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntityLiving() instanceof EntityPlayer && event.getItem().getItem() == item)
			((EntityPlayer) event.getEntityLiving()).addStat(this);
	}

}