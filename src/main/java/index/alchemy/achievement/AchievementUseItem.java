package index.alchemy.achievement;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.util.Tool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AchievementUseItem extends AlchemyAchievement implements IEventHandle {
	
	private Class<? extends Item> item;
	
	public AchievementUseItem(String name, int column, int row, Item icon, Achievement parent) {
		this(name, column, row, icon, icon.getClass(), parent);
	}
	
	public AchievementUseItem(String name, int column, int row, Item icon, Class<? extends Item> clazz, Achievement parent) {
		super(name, column, row, icon, parent);
		item = clazz;
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent
	public void onLivingEntityUseItemEventFinish(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntityLiving() instanceof EntityPlayer && Tool.isSubclass(item, event.getItem().getItem().getClass()))
			((EntityPlayer) event.getEntityLiving()).addStat(this);
	}

}