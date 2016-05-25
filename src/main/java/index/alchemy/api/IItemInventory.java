package index.alchemy.api;

import index.alchemy.item.ItemInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IItemInventory {
	
	public ItemInventory getItemInventory(EntityPlayer player, ItemStack item);
	
	public String getInventoryUnlocalizedName();
	
}
