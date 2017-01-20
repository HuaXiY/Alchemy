package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.Listener;
import net.minecraft.init.MobEffects;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Listener
public class ExPotion {
	
	@SubscribeEvent
	public static void onLivingHeal(LivingHealEvent event) {
		if (event.getEntityLiving().isPotionActive(MobEffects.WITHER))
			event.setCanceled(true);
	}

}
