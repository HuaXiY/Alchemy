package index.alchemy.potion;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionEternal extends AlchemyPotion implements IEventHandle {

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.getEntityLiving().isPotionActive(this))
			event.setAmount(Math.min(event.getEntityLiving().getHealth() - 0.01F, event.getAmount()));
	}
	
	public PotionEternal() {
		super("eternal", false, 0xFBD860);
	}

}