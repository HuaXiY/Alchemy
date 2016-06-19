package index.alchemy.inventory;

import java.util.RandomAccess;

import baubles.api.IBauble;
import index.alchemy.api.Alway;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageNBTUpdate;
import index.alchemy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryBauble extends AlchemyInventory implements ICapabilityProvider, INBTSerializable, RandomAccess {
	
	protected static final String CONTENTS = "bauble_contents";
	
	public static final int SIZE = 4;
	
	protected EntityLivingBase living;
	protected ItemStack[] contents = new ItemStack[SIZE];
	
	public InventoryBauble(final EntityLivingBase living) {
		this.living = living;
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList(CONTENTS, NBT.TAG_COMPOUND);
		if (!list.hasNoTags())
			contents = NBTHelper.getItemStacksFormNBTList(list);
		update(true);
	}
	
	public NBTTagCompound saveToNBT(NBTTagCompound nbt) {
		nbt.setTag(CONTENTS, NBTHelper.getNBTListFormItemStacks(contents));
		return nbt;
	}
	
	@Override
	public void markDirty() {}
	
	public boolean hasBauble() {
		int flag = 0;
		for (ItemStack item : contents)
			if (item != null)
				flag++;
		return flag > 0;
	}
	
	public void update(boolean init) {
		if (Alway.isServer() && (!init || hasBauble()))
			updateTracker();
	}
	
	public void updateTracker() {
		final NBTTagCompound data = saveToNBT(new NBTTagCompound());
		for (EntityPlayer player : ((WorldServer) living.worldObj).getEntityTracker().getTrackingPlayers(living))
			updatePlayer((EntityPlayerMP) player, data);
		if (living instanceof EntityPlayerMP)
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					updatePlayer((EntityPlayerMP) living, data);					
				}
			}, 1);
	}
	
	public void updatePlayer(EntityPlayerMP player) {
		updatePlayer(player, saveToNBT(new NBTTagCompound()));
	}
	
	public void updatePlayer(EntityPlayerMP player, NBTTagCompound data) {
		AlchemyNetworkHandler.updateEntityNBT(MessageNBTUpdate.Type.ENTITY_BAUBLE_DATA, living.getEntityId(), data, player);
	}
	
	public void copy(EntityLivingBase living) {
		living.getCapability(AlchemyCapabilityLoader.bauble, null).readFromNBT(saveToNBT(new NBTTagCompound()));
	}
	
	public EntityLivingBase getLiving() {
		return living;
	}
	
	public void updateNBT() { }

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
		ItemStack item = ItemStackHelper.getAndSplit(contents, index, count);
		if (item != null) {
			change(item, null);
			update(false);
		}
		return item;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack item = ItemStackHelper.getAndRemove(contents, index);
		if (item != null) {
			change(item, null);
			update(false);
		}
		return item;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		ItemStack old = contents[index];
		contents[index] = item;
		if (old != item) {
			change(old, item);
			update(false);
		}
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
		for (int i = 0; i < contents.length; i++) {
			change(contents[i], null);
			contents[i] = null;
		}
		update(false);
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

	@Override
	public NBTBase serializeNBT() {
		return AlchemyCapabilityLoader.bauble.getStorage().writeNBT(AlchemyCapabilityLoader.bauble, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		AlchemyCapabilityLoader.bauble.getStorage().readNBT(AlchemyCapabilityLoader.bauble, this, null, nbt);
	}

}
