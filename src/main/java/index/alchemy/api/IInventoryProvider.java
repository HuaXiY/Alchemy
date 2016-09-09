package index.alchemy.api;

import index.alchemy.inventory.AlchemyInventory;
import net.minecraft.item.ItemStack;

public interface IInventoryProvider<T> {
	
	public AlchemyInventory initInventory(T provider);
	
	public AlchemyInventory getInventory(T provider);
	
	public String getInventoryUnlocalizedName();
	
	public interface ItemProvider extends IInventoryProvider<ItemStack> { }
	
}
