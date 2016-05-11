package index.alchemy.potion;

import index.alchemy.entity.AlchemyDamageSourceLoader;
import net.minecraft.entity.EntityLivingBase;

public class PotionSoulWithered extends AlchemyPotion {
	
	@Override
	public boolean isReady(int tick, int level)
    {
        return tick % 20 == 0 || tick > 0;
    }
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		level = Math.max(level + 1, 1);
		float hp = living.getHealth(), max_hp = living.getMaxHealth();
		living.attackEntityFrom(AlchemyDamageSourceLoader.soul_withred, Math.max(hp * 0.02F * level, max_hp * 0.01F + level));
	}

	public PotionSoulWithered() {
		super("soul_withered", true, 0x000000);
	}
	
}