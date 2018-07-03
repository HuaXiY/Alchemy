package index.alchemy.inventory;

import java.util.RandomAccess;
import java.util.stream.Stream;

import index.alchemy.api.IItemHandlerModifiableInventory;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.util.InventoryHelper;
import index.alchemy.util.NBTHelper;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import static java.lang.Math.*;

@Omega
public abstract class AlchemyInventory implements ICapabilitySerializable<NBTTagCompound>, IItemHandlerModifiableInventory, RandomAccess {
	
	public static final int LIMIT = 64;
	public static final String CONTENTS = "contents";
	
	protected String name;
	protected int limit;
	protected NonNullList<ItemStack> contents;
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
		contents = NonNullList.withSize(size, ItemStack.EMPTY);
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
	public ItemStack getStackInSlot(int slot) {
		return contents.get(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return contents.get(slot).getCount();
	}

	@Override
	public boolean isEmpty() {
		return !contents.stream().filter(item -> item != ItemStack.EMPTY).findFirst().isPresent();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}
	
	protected IInventory getInv() {
		return this;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		ItemStack stackInSlot = getInv().getStackInSlot(slot);
		int m;
		if (!stackInSlot.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
				return stack;
			if (!getInv().isItemValidForSlot(slot, stack))
				return stack;
			m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();
			if (stack.getCount() <= m) {
				if (!simulate) {
					ItemStack copy = stack.copy();
					copy.grow(stackInSlot.getCount());
					getInv().setInventorySlotContents(slot, copy);
					getInv().markDirty();
				}
				return ItemStack.EMPTY;
			} else {
				// copy the stack to not modify the original one
				stack = stack.copy();
				if (!simulate) {
					ItemStack copy = stack.splitStack(m);
					copy.grow(stackInSlot.getCount());
					getInv().setInventorySlotContents(slot, copy);
					getInv().markDirty();
					return stack;
				} else {
					stack.shrink(m);
					return stack;
				}
			}
		} else {
			if (!getInv().isItemValidForSlot(slot, stack))
				return stack;
			m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
			if (m < stack.getCount()) {
				// copy the stack to not modify the original one
				stack = stack.copy();
				if (!simulate) {
					getInv().setInventorySlotContents(slot, stack.splitStack(m));
					getInv().markDirty();
					return stack;
				} else {
					stack.shrink(m);
					return stack;
				}
			} else {
				if (!simulate) {
					getInv().setInventorySlotContents(slot, stack);
					getInv().markDirty();
				}
				return ItemStack.EMPTY;
			}
		}
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;
		ItemStack stackInSlot = getInv().getStackInSlot(slot);
		if (stackInSlot.isEmpty())
			return ItemStack.EMPTY;
		if (simulate) {
			if (stackInSlot.getCount() < amount) {
				return stackInSlot.copy();
			} else {
				ItemStack copy = stackInSlot.copy();
				copy.setCount(amount);
				return copy;
			}
		} else {
			int m = Math.min(stackInSlot.getCount(), amount);
			ItemStack decrStackSize = getInv().decrStackSize(slot, m);
			getInv().markDirty();
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
		return contents.size();
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
		contents.set(index, item);
	}
	
	@Override
	public ItemStack getInventorySlotContents(int index) {
		return contents.get(index);
	}

	@Override
	public int getInventoryStackLimit() {
		return limit;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}
	
	@Override
	public void clear() {
		markDirty();
		for (int i = 0; i < contents.size(); i++)
			contents.set(i, ItemStack.EMPTY);
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
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return hasCapability(capability, facing) ? (T) this : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag(CONTENTS, NBTHelper.getNBTListFormItemStacks(contents.toArray(new ItemStack[contents.size()])));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (nbt instanceof NBTTagCompound) {
			NBTTagList list = ((NBTTagCompound) nbt).getTagList(CONTENTS, NBT.TAG_COMPOUND);
			if (!list.hasNoTags()) {
				ItemStack items[] = NBTHelper.getItemStacksFormNBTList(list);
				contents = NonNullList.withSize(items.length, ItemStack.EMPTY);
				for (int i = 0; i < items.length; i++)
					contents.set(i, items[i]);
			}
		}
	}
	
	public boolean hasItem() {
		for (ItemStack item : contents)
			if (!item.isEmpty())
				return true;
		return false;
	}
	
	public void mergeItemStack(ItemStack item) {
		if (item.isEmpty() || item.getCount() < 1)
			return;
		int limit = min(item.getMaxStackSize(), getInventoryStackLimit()), t;
		for (int i = 0, len = getSizeInventory(); i < len; i++) {
			ItemStack current = getStackInSlot(i);
			if (current.isEmpty()) {
				setInventorySlotContents(i, current = item.copy());
				item.setCount(0);
			}
			if (InventoryHelper.canMergeItemStack(item, current)) {
				current.setCount(current.getCount() + (t = min(limit - current.getCount(), item.getCount())));
				item.setCount(item.getCount() - t);
			}
			if (item.isEmpty())
				return;
		}
	}
	
	public Stream<ItemStack> stream() {
		return contents.stream();
	}
	
}
