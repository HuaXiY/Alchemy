package index.alchemy.item;

import java.util.UUID;

import index.alchemy.api.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static java.lang.Math.*;

public class ItemBeltTough extends AlchemyItemBelt implements IEventHandle {
	
	public static final int RECOVERY_INTERVAL = 20 * 8;
	public static final float BALANCE_COEFFICIENT = 0.4F, MIN_HEALTH = 0.01F;;
	
	public static final AttributeModifier KNOCKBACK_RESISTANCE =  
			new AttributeModifier(UUID.fromString("1434a694-1beb-4825-854b-72303432eed3"), "belt_tough_bonus", 1D, 0);
	static {
		((RangedAttribute) SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setShouldWatch(true);
	}
	
	@Override
	public void onEquipped(ItemStack item, EntityLivingBase living) {
		if (Always.isServer())
			living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(KNOCKBACK_RESISTANCE);
	}
	
	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {
		if (Always.isServer())
			living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE);
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0)
			living.heal(1F);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLivingHurt(LivingHurtEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (isEquipmented(living))
			event.setAmount(event.getAmount() * (1 - (1 - living.getHealth() / living.getMaxHealth()) * BALANCE_COEFFICIENT) - 1);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingDeath(LivingDeathEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (isEquipmented(living) && living.rand.nextInt((int) max(-living.getHealth(), 2)) == 0) {
			event.setCanceled(true);
			living.setHealth(MIN_HEALTH);
		}
	}

	public ItemBeltTough() {
		super("belt_tough", 0x63981E);
	}

}