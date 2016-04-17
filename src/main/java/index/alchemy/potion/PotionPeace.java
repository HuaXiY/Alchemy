package index.alchemy.potion;

import java.lang.reflect.Field;

import com.google.common.base.Predicate;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.entity.ai.EntityAIFindEntityNearestHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionPeace extends AlchemyPotion implements IEventHandle {
	
	public static Predicate<EntityPlayer> isPotion = new Predicate<EntityPlayer>() {
        public boolean apply(EntityPlayer player) {
            return !player.isPotionActive(AlchemyPotionLoader.peace);
        }
    };
	
	private static Field attackTarget = EntityLiving.class.getDeclaredFields()[10];
	static {	
		attackTarget.setAccessible(true);
	}
	
	public PotionPeace() {
		super("peace", false, 0xFFFFFF);
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.getEntityLiving() instanceof EntityLiving && event.getEntityLiving().isNonBoss() &&
			event.getTarget() != null && event.getTarget().isPotionActive(this) &&
			event.getEntityLiving().getCombatTracker().func_94550_c() != event.getTarget()) {
			EntityPlayer player = EntityAIFindEntityNearestHelper.<EntityPlayer>findNearest(
					(EntityLiving) event.getEntityLiving(), EntityPlayer.class, isPotion);
			try {
				attackTarget.set(event.getEntityLiving(), null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
