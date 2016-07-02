package index.alchemy.magic;

import index.alchemy.api.Alway;
import net.minecraft.entity.EntityLivingBase;

public class MagicBurning extends AlchemyMagic {
	
	public MagicBurning() {
		setStrength(8F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Alway.isServer())
			living.fire = (int) (strength * amplify);
	}

}