package index.alchemy.api;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IItemHandlerModifiableInventory extends IItemHandlerModifiable, IInventory {
	
	ItemStack getInventorySlotContents(int slot);
	
	@Override
	default ItemStack getStackInSlot(int slot) { return getInventorySlotContents(slot); }
	
	/*  In class <net.minecraft.inventory.IInventory>: mcp name<getStackInSlot> <=> srg name<func_70301_a>
	 *  
	 *  forge method name<net.minecraftforge.items.IItemHandler#getStackInSlot> ==
	 *  mc method mcp name<net.minecraft.inventory.IInventory#getStackInSlot>
	 *  
	 *  After reobf
	 *  <<? extends index.alchemy.api.IItemHandlerModifiableInventory>#getStackInSlot> ->
	 *  <<? extends index.alchemy.api.IItemHandlerModifiableInventory>#func_70301_a>
	 *  
	 *  An NoSuchMethodError will occur when <net.minecraftforge.items.IItemHandler#getStackInSlot> is called.
	 *  
	 *  After alchemy patch
	 *  <index.alchemy.api.IItemHandlerModifiableInventory> += <ItemStack getStackInSlot(int slot)>
	*/
	@Hook.Provider
	@Patch("index.alchemy.api.IItemHandlerModifiableInventory")
	interface Patch$IItemHandlerModifiableInventory extends IItemHandler {
		
		default ItemStack getStackInSlot(int slot) { return null; }
		
		// Avoid IncompatibleClassChangeError
		@Patch.Exception
		@Hook("index.alchemy.api.IItemHandlerModifiableInventory#getStackInSlot")
		static Hook.Result getStackInSlot(IItemHandlerModifiableInventory handler, int slot) {
			return new Hook.Result(handler.getInventorySlotContents(slot));
		}
		
	}
	
}
