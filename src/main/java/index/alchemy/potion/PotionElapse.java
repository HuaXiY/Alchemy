package index.alchemy.potion;

import java.util.function.Predicate;

import index.alchemy.api.IEventHandle;
import index.alchemy.entity.ai.EntityAIFindEntityNearestHelper;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Alpha
public class PotionElapse extends AlchemyPotion implements IEventHandle {
	
	public static final Predicate<EntityLivingBase> NOT_ACTIVE = l -> !l.isPotionActive(AlchemyPotionLoader.elapse);
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.getEntityLiving() instanceof EntityLiving && event.getEntityLiving().isNonBoss()
			&& event.getTarget() != null && event.getTarget().isPotionActive(this)
			&& event.getEntityLiving().getCombatTracker().getBestAttacker() != event.getTarget()) {
			EntityLiving living = (EntityLiving) event.getEntityLiving();
			Class<EntityLivingBase> type = (Class<EntityLivingBase>) 
					(event.getEntityLiving() instanceof EntityPlayer ? EntityPlayer.class : event.getEntityLiving().getClass());
			living.attackTarget = EntityAIFindEntityNearestHelper.<EntityLivingBase>findNearest(
					(EntityLiving) living.attackTarget, type, null, NOT_ACTIVE
					.and(l -> EntityAIFindEntityNearestHelper.isSuitableLivingTarget(living, l)));
		}
	}
	
	public PotionElapse() {
		super("elapse", false, 0xFFCCFF);
	}

}