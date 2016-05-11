package index.alchemy.potion;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionEternal extends AlchemyPotion implements IEventHandle {
	
	public static final float MIN_HEALTH = 0.01F;

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntityLiving().isPotionActive(this)) {
			event.setCanceled(true);
			event.getEntityLiving().setHealth(MIN_HEALTH);
		}
	}
	
	public PotionEternal() {
		super("eternal", false, 0xFBD860);
	}

}