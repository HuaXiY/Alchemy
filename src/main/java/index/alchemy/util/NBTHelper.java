package index.alchemy.util;

import index.project.version.annotation.Omega;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@Omega
public interface NBTHelper {
	
	static NBTTagCompound getOrSetNBT(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null)
			item.setTagCompound(nbt = new NBTTagCompound());
		return nbt;
	}
	
	static NBTTagCompound getNBTFormItemStack(ItemStack item) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (item == null)
			return nbt;
		item.writeToNBT(nbt);
		return nbt;
	}
	
	static NBTTagList getNBTListFormItemStacks(ItemStack[] items) {
		NBTTagList list = new NBTTagList();
		for (ItemStack item : items)
			list.appendTag(getNBTFormItemStack(item));
		return list;
	}
	
	static ItemStack getItemStackFormNBT(NBTTagCompound nbt) {
		return nbt.hasNoTags() ? null : ItemStack.loadItemStackFromNBT(nbt);
	}
	
	static ItemStack[] getItemStacksFormNBTList(NBTTagList list) {
		ItemStack[] item = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++)
			item[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
		return item;
	}
	
}
