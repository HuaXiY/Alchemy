package index.alchemy.magic;

import index.alchemy.api.Alway;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;

public class MagicLightning extends AlchemyMagic {
	
	@Override
	public boolean hasStrength() {
		return false;
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Alway.isServer())
			living.worldObj.addWeatherEffect(new EntityLightningBolt(living.worldObj, living.posX, living.posY, living.posZ, false));
	}

}