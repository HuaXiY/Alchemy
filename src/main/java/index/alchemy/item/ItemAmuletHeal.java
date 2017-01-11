package index.alchemy.item;

import index.alchemy.api.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.project.version.annotation.Omega;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class ItemAmuletHeal extends AlchemyItemAmulet implements IEventHandle {
	
	public static final float AMPLIFY = 0.5F;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		living.fire = 0;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHeal(LivingHealEvent event) {
		if (isEquipmented(event.getEntityLiving()))
			event.setAmount(event.getAmount() * (1 + AMPLIFY));
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		if (isEquipmented(event.getEntityLiving()) && event.getSource().isFireDamage())
			event.setCanceled(true);
	}

	public ItemAmuletHeal() {
		super("amulet_heal", 0xFF0033);
	}

}
