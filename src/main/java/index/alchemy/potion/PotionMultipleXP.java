package index.alchemy.potion;

import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

public class PotionMultipleXP extends AlchemyPotion {
	
	public PotionMultipleXP() {
		super("multiple_xp", false, 0x00FF7F);
	}
	
	public void onPlayerPickupXP(PlayerPickupXpEvent event) {
		PotionEffect effect = event.getEntityPlayer().getActivePotionEffect(AlchemyPotionLoader.multiple_xp);
		if (effect != null)
			event.getOrb().xpValue *= effect.getAmplifier() + 1;
	}

}