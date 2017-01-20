package index.alchemy.item;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.IItemTemperature;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.project.version.annotation.Omega;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class ItemAmuletHeal extends AlchemyItemAmulet implements IEventHandle, IItemTemperature {
	
	public static final float AMPLIFY = 0.5F, TEMPERATURE_RATE = 100F, TEMPERATURE_TARGET = 4F;
	
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
	
	@Override
	public float modifyChangeRate(World world, EntityPlayer player, float changeRate, int trend) {
		return trend < 0 ? changeRate + TEMPERATURE_RATE : changeRate;
	}

	@Override
	public float modifyTarget(World world, EntityPlayer player, float temperature) {
		return temperature + TEMPERATURE_TARGET;
	}

	public ItemAmuletHeal() {
		super("amulet_heal", 0xFF0033);
	}

}
