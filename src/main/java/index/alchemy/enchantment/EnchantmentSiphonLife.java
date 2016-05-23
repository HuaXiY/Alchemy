package index.alchemy.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentSiphonLife extends AlchemyEnchantment {

	@Override
	public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
		user.heal(level + 1);
	}
	
	public EnchantmentSiphonLife() {
		super("siphon_life", Rarity.VERY_RARE, EnumEnchantmentType.WEAPON, 2, EntityEquipmentSlot.MAINHAND);
	}

}