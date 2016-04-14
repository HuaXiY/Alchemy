package index.alchemy.potion;

import java.lang.reflect.Field;

import com.google.common.base.Predicate;

import index.alchemy.entity.ai.EntityAIFindEntityNearestHelper;
import index.alchemy.util.Tool;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

public class PotionPeace extends AlchemyPotion {
	
	public static Predicate<EntityPlayer> isPotion = new Predicate<EntityPlayer>() {
        public boolean apply(EntityPlayer player) {
            return !player.isPotionActive(AlchemyPotionLoader.peace);
        }
    };
	
	private static Field attackTarget = EntityLiving.class.getDeclaredFields()[10];
	{	
		attackTarget.setAccessible(true);
	}
	
	public PotionPeace() {
		super("peace", false, 0xFFFFFF);
	}
	
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.getEntityLiving() instanceof EntityLiving && event.getTarget() != null &&
			event.getEntityLiving().getCombatTracker().func_94550_c() != event.getTarget() &&
			event.getTarget().isPotionActive(AlchemyPotionLoader.peace)) {
			EntityPlayer player = EntityAIFindEntityNearestHelper.<EntityPlayer>findNearest(
					(EntityLiving) event.getEntityLiving(), EntityPlayer.class, isPotion);
			try {
				attackTarget.set(event.getEntityLiving(), null);
			} catch (Exception e) { e.printStackTrace(); }
		}
	}

}
