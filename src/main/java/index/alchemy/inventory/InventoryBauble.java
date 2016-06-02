package index.alchemy.inventory;

import java.util.RandomAccess;

import baubles.api.IBauble;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;

public class InventoryBauble extends AlchemyInventory implements ICapabilityProvider, RandomAccess {
	
	protected static final String CONTENTS = "bauble_contents";
	
	public static final int SIZE = 4;
	
	protected EntityLivingBase living;
	protected ItemStack[] contents;
	
	public InventoryBauble(EntityLivingBase living) {
		this.living = living;
		init(living);
		if (contents == null)
			contents = new ItemStack[SIZE];
	}
	
	protected void init(EntityLivingBase living) {
		NBTTagCompound nbt = living.getEntityData();
		if (nbt != null) {
			NBTTagList list = nbt.getTagList(CONTENTS, NBT.TAG_COMPOUND);
			if (!list.hasNoTags())
				contents = NBTHelper.getItemStacksFormNBTList(list);
		}
	}
	
	public EntityLivingBase getLiving() {
		return living;
	}
	
	public void updateNBT() {
		NBTTagCompound nbt = living.getEntityData();
		nbt.setTag(CONTENTS, NBTHelper.getNBTListFormItemStacks(contents));
	}

	@Override
	public int getSizeInventory() {
		return SIZE;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return contents[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		markDirty();
		ItemStack item = ItemStackHelper.getAndSplit(contents, index, count);
		change(item, null);
		return item;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		markDirty();
		ItemStack item = ItemStackHelper.getAndRemove(contents, index);
		change(item, null);
		return item;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		markDirty();
		change(contents[index], item);
		contents[index] = item;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer living) {
		return this.living == living;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack item) {
		return item != null && item.getItem() instanceof IBauble;
	}

	@Override
	public void clear() {
		markDirty();
		for (int i = 0; i < contents.length; i++) {
			change(contents[i], null);
			contents[i] = null;
		}
	}
	
	public void change(ItemStack old, ItemStack _new) {
		if (old !=null)
			((IBauble) old.getItem()).onUnequipped(old, living);
		if (_new !=null)
			((IBauble) _new.getItem()).onEquipped(_new, living);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return AlchemyCapabilityLoader.bauble == capability;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (hasCapability(capability, facing))
			return (T) this;
		return null;
	}

}
