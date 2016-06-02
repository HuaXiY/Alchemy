package index.alchemy.item;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import index.alchemy.api.Alway;
import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class ItemBeltTough extends AlchemyItemBelt implements IEventHandle {
	
	public static final int RECOVERY_INTERVAL = 20 * 6;
	public static final float BALANCE_COEFFICIENT = 0.4F;
	
	public static final AttributeModifier KNOCKBACK_RESISTANCE =  
			new AttributeModifier(UUID.fromString("1434a694-1beb-4825-854b-72303432eed3"), "belt_tough_bonus", 1D, 0);
	
	@Override
	public void onEquipped(ItemStack item, EntityLivingBase living) {
		System.out.println(living);
		System.out.println("onE");
		for (AttributeModifier modifier : living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getModifiers())
			System.out.println(modifier);
		living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(KNOCKBACK_RESISTANCE);
	}
	
	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {
		System.out.println("onU");
		for (AttributeModifier modifier : living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getModifiers())
			System.out.println(modifier);
		living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE);
		for (AttributeModifier modifier : living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getModifiers())
			System.out.println(modifier);
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Alway.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0)
			living.heal(1F);
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHurt(LivingHurtEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (isEquipmented(living))
			event.setAmount(event.getAmount() * (1 - (1 - living.getHealth() / living.getMaxHealth()) * BALANCE_COEFFICIENT));
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDrops(PlayerDropsEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		System.out.println("onD");
		for (AttributeModifier modifier : player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getModifiers())
			System.out.println(modifier);
		if (!isEquipmented(player))
			onUnequipped(null, player);
	}

	public ItemBeltTough() {
		super("belt_tough", 0x00CC00);
	}

}