package index.alchemy.potion;

import index.alchemy.api.IEventHandle;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class PotionEternal extends AlchemyPotion implements IEventHandle {
	
	public static final float MIN_HEALTH = 0.01F;
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if (Always.isServer() && event.getEntityLiving().isPotionActive(this)) {
			event.setCanceled(true);
			event.getEntityLiving().setHealth(MIN_HEALTH);
		}
	}
	
	public PotionEternal() {
		super("eternal", false, 0xFBD451);
	}

}