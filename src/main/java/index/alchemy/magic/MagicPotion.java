package index.alchemy.magic;

import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

@Alpha
public class MagicPotion extends AlchemyMagic {
	
	protected PotionEffect effect;
	
	public MagicPotion() {
		setStrength(1F);
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		living.addPotionEffect(new PotionEffect(effect));
	}

}
