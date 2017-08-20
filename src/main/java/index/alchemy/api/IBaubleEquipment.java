package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IBaubleEquipment {
	
	default boolean isEquipmented(EntityLivingBase living) {
		return getFormLiving(living).getItem() == this;
	}
	
	@Nullable
	ItemStack getFormLiving(EntityLivingBase living);

}
