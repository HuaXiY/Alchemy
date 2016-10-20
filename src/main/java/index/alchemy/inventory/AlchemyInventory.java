package index.alchemy.inventory;

import java.util.RandomAccess;

import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.util.InventoryHelper;
import index.alchemy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import static java.lang.Math.*;

public abstract class AlchemyInventory implements ICapabilitySerializable, IInventory, IItemHandlerModifiable, RandomAccess {
	
	public static final int LIMIT = 64;
	public static final String CONTENTS = "contents";
	
	protected String name;
	protected int limit;
	protected ItemStack[] contents;
	protected boolean dirty;
	
	public AlchemyInventory(int size) {
		this(size, "", LIMIT);
	}
	
	public AlchemyInventory(int size, String name) {
		this(size, name, 64);
	}
	
	public AlchemyInventory(int size, String name, int limit) {
		this.name = name;
		this.limit = limit;
		contents = new ItemStack[size];
	}
	
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
	public void openInventory(EntityPlayer living) { }

	@Override
	public void closeInventory(EntityPlayer living) { }
	
	@Override
	public String getName() {
		return name;
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
	public int getSizeInventory() {
		return contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return contents[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		markDirty();
		return ItemStackHelper.getAndSplit(contents, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		markDirty();
		return ItemStackHelper.getAndRemove(contents, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		markDirty();
		contents[index] = item;
	}

	@Override
	public int getInventoryStackLimit() {
		return limit;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}
	
	@Override
	public void clear() {
		markDirty();
		for (int i = 0; i < contents.length; i++)
			contents[i] = null;
	}
	
	@Override
	public int getField(int id) { return 0; }

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() { return 0; }

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == AlchemyCapabilityLoader.inventory;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return hasCapability(capability, facing) ? (T) this : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(CONTENTS, NBTHelper.getNBTListFormItemStacks(contents));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		if (nbt instanceof NBTTagCompound) {
			NBTTagList list = ((NBTTagCompound) nbt).getTagList(CONTENTS, NBT.TAG_COMPOUND);
			if (!list.hasNoTags())
				contents = NBTHelper.getItemStacksFormNBTList(list);
		}
	}
	
	public boolean hasItem() {
		for (ItemStack item : contents)
			if (item != null)
				return true;
		return false;
	}
	
	public void mergeItemStack(ItemStack item) {
		if (item == null || item.stackSize < 1)
			return;
		int limit = min(item.getMaxStackSize(), getInventoryStackLimit()), t;
		for (int i = 0, len = getSizeInventory(); i < len; i++) {
			ItemStack current = getStackInSlot(i);
			if (current == null) {
				setInventorySlotContents(i, current = item.copy());
				current.stackSize = 0;
			}
			if (InventoryHelper.canMergeItemStack(item, current)) {
				current.stackSize += t = min(limit - current.stackSize, item.stackSize);
				item.stackSize -= t;
			}
			if (item.stackSize < 1)
				return;
		}
	}
	
}
