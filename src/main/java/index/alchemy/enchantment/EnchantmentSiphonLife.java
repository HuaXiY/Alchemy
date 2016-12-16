package index.alchemy.enchantment;

import index.alchemy.api.IEventHandle;
import index.project.version.annotation.Beta;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static java.lang.Math.*;

@Beta
public class EnchantmentSiphonLife extends AlchemyEnchantment implements IEventHandle {
	
	public static final float SIPHON_COEFFICIENT = 0.05F;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.getSource() instanceof EntityDamageSource && !(event.getSource() instanceof EntityDamageSourceIndirect) &&
				event.getSource().getEntity() instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) event.getSource().getEntity();
			int level = EnchantmentHelper.getEnchantmentLevel(this, living.getHeldItemMainhand());
			if (level > 0)
				living.heal(event.getAmount() * min(SIPHON_COEFFICIENT * level, 1F));
		}
	}
	
	public EnchantmentSiphonLife() {
		super("siphon_life", Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, 4, EntityEquipmentSlot.MAINHAND);
	}

}
