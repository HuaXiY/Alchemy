package index.alchemy.potion;

import index.project.version.annotation.Beta;
import net.minecraft.entity.EntityLivingBase;

@Beta
public class PotionFeatherFall extends AlchemyPotion {
	
	@Override
	public boolean isReady(int tick, int level) {
		return true;
	}
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		if (!living.onGround && living.motionY < 0) {
			living.motionY *= 0.75;
			living.fallDistance = 0;
		}
	}
	
	public PotionFeatherFall() {
		super("feather_fall", false, 0xCCFFFF);
	}

}