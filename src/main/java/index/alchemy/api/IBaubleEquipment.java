package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IBaubleEquipment {
	
	public boolean isEquipmented(EntityLivingBase living);
	
	@Nullable
	public ItemStack getFormLiving(EntityLivingBase living);

}
