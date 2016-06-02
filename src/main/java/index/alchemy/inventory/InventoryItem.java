package index.alchemy.inventory;

import java.util.RandomAccess;

import com.google.common.base.Objects;

import index.alchemy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class InventoryItem extends AlchemyInventory implements RandomAccess {
	
	protected static final String CONTENTS = "item_contents";
	
	protected final ItemStack content;
	protected EntityPlayer player;
	protected ItemStack[] contents;
	
	protected String name; 
	protected int size;
	
	public InventoryItem(EntityPlayer player, ItemStack content, int size, String name) {
		this.player = player;
		this.content = content;
		this.size = size;
		this.name = name;
		init(content);
		if (contents == null)
			contents = new ItemStack[size];
	}
	
	protected void init(ItemStack item) {
		NBTTagCompound nbt = content.getTagCompound();
		if (nbt != null) {
			NBTTagList list = nbt.getTagList(CONTENTS, NBT.TAG_COMPOUND);
			if (!list.hasNoTags())
				contents = NBTHelper.getItemStacksFormNBTList(list);
		}
	}
	
	public void mergeItemStack(ItemStack item) {
		if (item == null || item.stackSize < 1)
			return;
		int limit = item.getMaxStackSize(), t;
		for (int i = 0, len = getSizeInventory(); i < len; i++) {
			ItemStack current = getStackInSlot(i);
			if (current != null) {
				if (current.stackSize < limit &&
					current.getItem() == item.getItem() &&
					current.getMaxDamage() <= 0 &&
					current.getItemDamage() == item.getItemDamage() &&
					current.getMetadata() == item.getMetadata() &&
					Objects.equal(current.getTagCompound(), item.getTagCompound())) {
					current.stackSize += t = Math.min(limit - current.stackSize, item.stackSize);
					item.stackSize -= t;
				}
			} else {
				setInventorySlotContents(i, item.copy());
				item.stackSize = 0;
			}
			if (item.stackSize < 1)
				return;
		}
	}
	
	public boolean nonUsing() {
		return player == null;
	}
	
	public boolean isUsing() {
		return player != null;
	}
	
	public void updateNBT() {
		NBTTagCompound nbt = content.getTagCompound();
		if (nbt == null)
			content.setTagCompound(nbt = new NBTTagCompound());
		nbt.setTag(CONTENTS, NBTHelper.getNBTListFormItemStacks(contents));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getSizeInventory() {
		return size;
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
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.player == player;
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
	
}
