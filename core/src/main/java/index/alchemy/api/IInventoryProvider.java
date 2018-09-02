package index.alchemy.api;

import index.alchemy.inventory.AlchemyInventory;

public interface IInventoryProvider<T> {
    
    AlchemyInventory initInventory();
    
    AlchemyInventory getInventory(T provider);
    
    String getInventoryUnlocalizedName();
    
}
