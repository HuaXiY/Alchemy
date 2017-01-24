package index.alchemy.api;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IItemHandlerModifiableInventory extends IItemHandlerModifiable, IInventory {
	
	ItemStack getInventorySlotContents(int slot);
	
	default ItemStack getStackInSlot(int slot) { return getInventorySlotContents(slot); }
	
	@Hook.Provider
	@Patch("index.alchemy.api.IItemHandlerModifiableInventory")
	interface Patch$IItemHandlerModifiableInventory extends IItemHandler {
		
		default ItemStack getStackInSlot(int slot) { return null; }
		
		@Hook("index.alchemy.api.IItemHandlerModifiableInventory#getStackInSlot")
		static Hook.Result getStackInSlot(IItemHandlerModifiableInventory handler, int slot) {
			return new Hook.Result(handler.getInventorySlotContents(slot));
		}
		
	}
	
}
