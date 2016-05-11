package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemAmuletHeal extends AlchemyItemAmulet implements IEventHandle {
	
	public static final float AMPLIFY = 0.2F;
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (Alway.isServer() && isEquipmented(event.getEntityLiving()) && event.getSource().isFireDamage())
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHeal(LivingHealEvent event) {
		if (Alway.isServer() && isEquipmented(event.getEntityLiving()))
			event.setAmount(event.getAmount() * (1 + AMPLIFY));
	}

	public ItemAmuletHeal() {
		super("amulet_heal", 0xFF0033);
	}

}
