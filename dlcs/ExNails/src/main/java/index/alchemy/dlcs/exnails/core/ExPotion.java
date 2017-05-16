package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.Listener;
import index.alchemy.core.AlchemyEventSystem;
import index.project.version.annotation.Omega;
import net.minecraft.init.MobEffects;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
@Listener
public class ExPotion {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingHeal(LivingHealEvent event) {
		if (event.getEntityLiving().isPotionActive(MobEffects.WITHER))
			AlchemyEventSystem.markEventCanceled(event);
	}

}
