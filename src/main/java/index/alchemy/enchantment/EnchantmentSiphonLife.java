package index.alchemy.enchantment;

import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnchantmentSiphonLife extends AlchemyEnchantment implements IEventHandle {
	
	public static final float SIPHON_COEFFICIENT = 0.1F;
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.getSource() instanceof EntityDamageSource && !(event.getSource() instanceof EntityDamageSourceIndirect) &&
				event.getSource().getEntity() instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) event.getSource().getEntity();
			int level = EnchantmentHelper.getEnchantmentLevel(this, living.getHeldItemMainhand());
			if (level > 0)
				living.heal(event.getAmount() * SIPHON_COEFFICIENT * level);
		}
	}
	
	public EnchantmentSiphonLife() {
		super("siphon_life", Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, 2, EntityEquipmentSlot.MAINHAND);
	}

}