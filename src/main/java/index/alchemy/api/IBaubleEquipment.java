package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IBaubleEquipment {
	
	public default boolean isEquipmented(EntityLivingBase living) {
		return getFormLiving(living) != null;
	}
	
	@Nullable
	public ItemStack getFormLiving(EntityLivingBase living);

}
