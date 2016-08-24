package index.alchemy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class AlchemyInventory implements IInventory, IItemHandlerModifiable {
	
	protected boolean dirty;
	
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public void markDirty() {
		dirty = true;
	}
	
	@Override
	public int getSlots() {
		return getSizeInventory();
	}
	
	@Override
    public ItemStack getStackInSlot(int slot) {
        return getStackInSlot(slot);
    }
	
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null)
            return null;

        if (!isItemValidForSlot(slot, stack))
            return stack;

        ItemStack stackInSlot = getStackInSlot(slot);

        int m;
        if (stackInSlot != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getInventoryStackLimit()) - stackInSlot.stackSize;

            if (stack.stackSize <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.stackSize += stackInSlot.stackSize;
                    setInventorySlotContents(slot, copy);
                    markDirty();
                }

                return null;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    copy.stackSize += stackInSlot.stackSize;
                    setInventorySlotContents(slot, copy);
                    markDirty();
                    return stack;
                } else {
                    stack.stackSize -= m;
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), getInventoryStackLimit());
            if (m < stack.stackSize) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    setInventorySlotContents(slot, stack.splitStack(m));
                    markDirty();
                    return stack;
                } else {
                    stack.stackSize -= m;
                    return stack;
                }
            } else {
                if (!simulate) {
                    setInventorySlotContents(slot, stack);
                    markDirty();
                }
                return null;
            }
        }
    }
    
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return null;

        ItemStack stackInSlot = getStackInSlot(slot);

        if (stackInSlot == null)
            return null;

        if (simulate) {
            if (stackInSlot.stackSize < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                copy.stackSize = amount;
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.stackSize, amount);

            ItemStack decrStackSize = decrStackSize(slot, m);
            markDirty();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        setInventorySlotContents(slot, stack);
    }
	
	@Override
	public void openInventory(EntityPlayer living) {}

	@Override
	public void closeInventory(EntityPlayer living) {
		if (isDirty())
			updateNBT();
		living = null;
	}
	
	public abstract void updateNBT();
	
	@Override
	public String getName() {
		return "";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}
	
	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}
	
}
