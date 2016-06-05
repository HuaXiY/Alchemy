package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.potion.AlchemyPotion;
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
		if (Alway.isServer() && living.fire > 0) {
			living.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, AlchemyPotion.NOT_FLASHING_TIME));
			living.fire = 0;
		}
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHeal(LivingHealEvent event) {
		if (Alway.isServer() && isEquipmented(event.getEntityLiving()))
			event.setAmount(event.getAmount() * (1 + AMPLIFY));
	}

	public ItemAmuletHeal() {
		super("amulet_heal", 0xFF0033);
	}

}
