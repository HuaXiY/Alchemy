package index.alchemy.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

public class PotionStable extends AlchemyPotion {
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		if (living instanceof EntityCreeper)
			((EntityCreeper) living).setCreeperState(-1);
	}

	public PotionStable() {
		super("stable", false, 0x66FF99);
	}

}
