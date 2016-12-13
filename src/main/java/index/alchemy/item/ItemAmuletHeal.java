package index.alchemy.item;

import index.alchemy.api.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.potion.AlchemyPotion;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemAmuletHeal extends AlchemyItemAmulet implements IEventHandle {
	
	public static final float AMPLIFY = 0.2F;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer() && living.fire > 0) {
			living.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, AlchemyPotion.NOT_FLASHING_TIME));
			living.fire = 0;
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHeal(LivingHealEvent event) {
		if (Always.isServer() && isEquipmented(event.getEntityLiving()))
			event.setAmount(event.getAmount() * (1 + AMPLIFY));
	}

	public ItemAmuletHeal() {
		super("amulet_heal", 0xFF0033);
	}

}
