package index.alchemy.potion;

import index.alchemy.api.Always;
import index.alchemy.util.AABBHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class PotionDangerSense extends AlchemyPotion {
	
	public static final int RANGE = 32, OUT_OF_RANGE = 108;
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		if (Always.isClient())
			if (living == Minecraft.getMinecraft().thePlayer) {
				for (EntityLivingBase oliving : living.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
						AABBHelper.getAABBFromEntity(living, RANGE), Always.IS_MONSTER))
					if (oliving != Minecraft.getMinecraft().thePlayer) {
						oliving.setGlowing(true);
						oliving.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 2));
					}
				for (EntityLivingBase oliving : living.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
						AABBHelper.getAABBFromEntity(living, OUT_OF_RANGE), Always.IS_MONSTER)) {
					PotionEffect effect = oliving.getActivePotionEffect(MobEffects.GLOWING);
					if (effect == null || effect.getDuration() == 0)
						oliving.setGlowing(false);
				}
			}
	}

	public PotionDangerSense() {
		super("danger_sense", false, 0xFFFFFF);
	}

}