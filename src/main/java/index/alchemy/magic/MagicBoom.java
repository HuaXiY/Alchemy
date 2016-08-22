package index.alchemy.magic;

import index.alchemy.api.Always;
import net.minecraft.entity.EntityLivingBase;

public class MagicBoom extends AlchemyMagic {
	
	public MagicBoom() {
		setStrength(1F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Always.isServer())
			living.worldObj.createExplosion(living, living.posX, living.posY, living.posZ, strength * amplify, true);
	}

}