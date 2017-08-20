package index.alchemy.magic;

import index.alchemy.util.Always;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLivingBase;

@Alpha
public class MagicBoom extends AlchemyMagic {
	
	public MagicBoom() {
		setStrength(1F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Always.isServer())
			living.world.createExplosion(living, living.posX, living.posY, living.posZ, strength * amplify, true);
	}

}