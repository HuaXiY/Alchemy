package index.alchemy.inventory;

import net.minecraft.item.ItemStack;

public class InventoryItem extends AlchemyInventory {
	
	protected final ItemStack content;
	
	public InventoryItem(ItemStack content, int size, String name) {
		super(size);
		this.content = content;
		this.name = name;
	}
	
}
