package index.alchemy.api;

import index.alchemy.inventory.InventoryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IItemInventory {
	
	public InventoryItem getItemInventory(EntityPlayer player, ItemStack item);
	
	public String getInventoryUnlocalizedName();
	
}
