package index.alchemy.potion;

import index.project.version.annotation.Omega;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

@Omega
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
