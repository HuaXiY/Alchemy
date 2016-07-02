package index.alchemy.magic;

import index.alchemy.api.Alway;
import index.alchemy.entity.ai.EntityTrackTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class MagicPotion extends AlchemyMagic {
	
	protected PotionEffect effect;
	
	public MagicPotion() {
		setStrength(1F);
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		//living.addPotionEffect(effect.);
	}

}
