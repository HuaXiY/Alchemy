package index.alchemy.magic;

import net.minecraft.entity.EntityLivingBase;

public class MagicGravity extends AlchemyMagic {
	
	public MagicGravity() {
		setStrength(2F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		living.motionY -= strength;
	}

}
