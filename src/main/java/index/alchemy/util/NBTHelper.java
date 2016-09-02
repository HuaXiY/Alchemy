package index.alchemy.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class NBTHelper {
	
	public static final NBTTagCompound getNBTFormItemStack(ItemStack item) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (item == null)
			return nbt;
		item.writeToNBT(nbt);
		return nbt;
	}
	
	public static final NBTTagList getNBTListFormItemStacks(ItemStack[] items) {
		NBTTagList list = new NBTTagList();
		for (ItemStack item : items)
			list.appendTag(getNBTFormItemStack(item));
		return list;
	}
	
	public static final ItemStack getItemStackFormNBT(NBTTagCompound nbt) {
		return ItemStack.loadItemStackFromNBT(nbt);
	}
	
	public static final ItemStack[] getItemStacksFormNBTList(NBTTagList list) {
		ItemStack[] item = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++)
			item[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
		return item;
	}
	
}
