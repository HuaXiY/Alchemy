package index.alchemy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBaubleEquipment {
	
	public boolean isEquipmented(EntityPlayer player);
	
	public ItemStack getFormPlayer(EntityPlayer player);

}
