package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemBeltGuard extends AlchemyItemBelt implements IEventHandle {
	
	public static final int RECOVERY_INTERVAL = 20 * 3, RECOVERY_CD = 20 * 6, MAX_ABSORPTION = 20;
	public static final float DECREASE = 0.8F;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Alway.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getAbsorptionAmount() < MAX_ABSORPTION
				&& living.ticksExisted - living.getLastAttackerTime() > RECOVERY_CD
				&& living.ticksExisted - living.getCombatTracker().lastDamageTime > RECOVERY_CD)
			living.setAbsorptionAmount(Math.min(living.getAbsorptionAmount() + 1, MAX_ABSORPTION));
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (isEquipmented(event.getEntityLiving()) && event.getEntityLiving().getAbsorptionAmount() > 0F)
			event.setAmount(event.getAmount() * DECREASE);
	}

	public ItemBeltGuard() {
		super("belt_guard", 0xFFCC00);
	}

}