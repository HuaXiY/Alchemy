package index.alchemy.potion;

import index.alchemy.entity.AlchemyDamageSourceLoader;
import index.project.version.annotation.Beta;
import net.minecraft.entity.EntityLivingBase;

@Beta
public class PotionDeadOrAlive extends AlchemyPotion {
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		float f = random.nextFloat() * living.getMaxHealth() * 2 - living.getMaxHealth();
		if (f > 0)
			living.heal(f);
		else 
			living.attackEntityFrom(AlchemyDamageSourceLoader.dead_magic, Math.min(living.getHealth() - 0.1F, -f));
	}
	
	public PotionDeadOrAlive() {
		super("dead_or_alive", false, 0xFF0066, true);
	}
	
}