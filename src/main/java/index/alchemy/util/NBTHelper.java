package index.alchemy.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class NBTHelper {
	
	public static NBTTagCompound getNBTFormItemStack(ItemStack item) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (item == null)
			return nbt;
		item.writeToNBT(nbt);
		return nbt;
	}
	
	public static NBTTagList getNBTListFormItemStacks(ItemStack[] items) {
		NBTTagList list = new NBTTagList();
		for (ItemStack item : items)
			list.appendTag(getNBTFormItemStack(item));
		return list;
	}
	
	public static ItemStack getItemStackFormNBT(NBTTagCompound nbt) {
		return ItemStack.loadItemStackFromNBT(nbt);
	}
	
	public static ItemStack[] getItemStacksFormNBTList(NBTTagList list) {
		ItemStack[] item = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++)
			item[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
		return item;
	}
	
}
