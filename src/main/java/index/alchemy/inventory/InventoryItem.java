package index.alchemy.inventory;

import index.project.version.annotation.Omega;

@Omega
public class InventoryItem extends AlchemyInventory {
	
	public InventoryItem(int size, String name) {
		super(size);
		this.name = name;
	}
	
}
