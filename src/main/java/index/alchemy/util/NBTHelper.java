package index.alchemy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import index.project.version.annotation.Omega;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
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
		return nbt.hasNoTags() ? null : new ItemStack(nbt);
	}
	
	static ItemStack[] getItemStacksFormNBTList(NBTTagList list) {
		ItemStack[] item = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++)
			item[i] = new ItemStack(list.getCompoundTagAt(i));
		return item;
	}
	
	static NBTTagCompound getNBTFromPlayerName(String name) {
		try {
			return CompressedStreamTools.readCompressed(new FileInputStream(new File(Always.getWorldDirectory(),
					"playerdata/" + Always.getUUIDFromPlayerName(name) + ".dat")));
		} catch (IOException e) { return new NBTTagCompound(); }
	}
	
}
