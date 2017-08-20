package index.alchemy.magic;

import index.alchemy.util.Always;
import index.project.version.annotation.Beta;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;

@Beta
public class MagicLightning extends AlchemyMagic {
	
	@Override
	public boolean hasStrength() {
		return false;
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Always.isServer())
			living.world.addWeatherEffect(new EntityLightningBolt(living.world, living.posX, living.posY, living.posZ, false));
	}

}