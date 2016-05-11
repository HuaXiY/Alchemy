package index.alchemy.potion;

import net.minecraft.entity.EntityLivingBase;

public class PotionFeatherFall extends AlchemyPotion {
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		if (!living.onGround && living.motionY < 0) {
			living.motionY *= 0.75;
			living.fallDistance = 0;
		}
	}
	
	public PotionFeatherFall() {
		super("feather_fall", false, 0xFFFFFF, true);
	}

}