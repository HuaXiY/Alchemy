package index.alchemy.inventory;

import java.util.Arrays;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import index.alchemy.api.AlchemyBaubles;
import index.alchemy.api.annotation.Texture;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageNBTUpdate;
import index.alchemy.util.Always;
import index.alchemy.util.InventoryHelper;
import index.alchemy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;

public class InventoryBauble extends AlchemyInventory implements IBaublesItemHandler {
	
	public static final int LIMIT = 1;
	public static final String NAME = "bauble", UPDATE_INDEX_NBT_KEY = "";
	
	@Texture({
		"alchemy:items/empty_ring",
		"alchemy:items/empty_amulet",
		"alchemy:items/empty_belt",
		"alchemy:items/empty_head",
		"alchemy:items/empty_body",
		"alchemy:items/empty_charm"
	})
	public static class SlotBauble extends Slot {
		
		public static final String EMPTY_NAME[] = SlotBauble.class.getAnnotation(Texture.class).value();
		
		private final BaubleType type;
		private final EntityLivingBase living;
		
		public SlotBauble(EntityLivingBase living, IInventory inventory, BaubleType type, int index, int x, int y) {
			super(inventory, index, x, y);
			this.living = living;
			this.type = type;
			setBackgroundName(EMPTY_NAME[type.ordinal()]);
		}
		
		public BaubleType getType() {
			return type;
		}
		
		@Override
		public boolean isItemValid(ItemStack item) {
			return item != null && item.getItem() != null &&
					item.getItem() instanceof IBauble && 
				   ((IBauble) item.getItem()).getBaubleType(item) == type &&
				   ((IBauble) item.getItem()).canEquip(item, living);
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return getStack() != null &&
				   ((IBauble) getStack().getItem()).canUnequip(getStack(), player);
		}
		
		@Override
		public int getSlotStackLimit() {
			return 1;
		}
		
	}
	
	protected EntityLivingBase living;
	protected boolean init, blockEvents = true, changed[];
	
	public InventoryBauble(final EntityLivingBase living) {
		super(AlchemyBaubles.getBaublesSize(), NAME, LIMIT);
		this.living = living;
		this.changed = new boolean[getSizeInventory()];
	}
	
	public boolean update(boolean init) {
		return Always.isServer() && (!init || hasItem()) && updateTracker(init ? serializeNBT() : getUpdateNBT());
	}
	
	public boolean updateTracker(NBTTagCompound data) {
		if (data != null) {
			AlchemyEventSystem.addDelayedRunnable(p -> {
				for (EntityPlayer player : ((WorldServer) living.worldObj).getEntityTracker().getTrackingPlayers(living))
					updatePlayer((EntityPlayerMP) player, data);
				if (living instanceof EntityPlayerMP)
					updatePlayer((EntityPlayerMP) living, data);
			}, 0);
			return true;
		}
		return false;
	}
	
	public void updatePlayer(EntityPlayerMP player, NBTTagCompound data) {
		AlchemyNetworkHandler.updateEntityNBT(MessageNBTUpdate.Type.ENTITY_BAUBLE_DATA, living.getEntityId(), data, player);
	}
	
	@Nullable
	public NBTTagCompound getUpdateNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		int index = ArrayUtils.indexOf(changed, true);
		if (index == -1)
			return null;
		changed[index] = false;
		nbt.setInteger(UPDATE_INDEX_NBT_KEY, index);
		nbt.setTag(CONTENTS, NBTHelper.getNBTFormItemStack(getStackInSlot(index)));
		return nbt;
	}
	
	public void updateItem(int index, ItemStack item) {
		contents[index] = item;
	}
	
	public void copy(EntityLivingBase living) {
		living.getCapability(AlchemyCapabilityLoader.bauble, null).deserializeNBT(serializeNBT());
	}
	
	public EntityLivingBase getLiving() {
		return living;
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack item = super.removeStackFromSlot(index);
		if (item != null) {
			change(item, null);
			setChanged(index, true);
			update(false);
		}
		return item;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack item = super.removeStackFromSlot(index);
		if (item != null) {
			change(item, null);
			setChanged(index, true);
		}
		return item;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		ItemStack old = contents[index];
		super.setInventorySlotContents(index, item);
		if (!InventoryHelper.areItemsEqual(old, item)) {
			change(old, item);
			setChanged(index, true);
		}
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
		super.clear();
		Arrays.fill(changed, true);
	}
	
	public void change(ItemStack old, ItemStack _new) {
		if (isEventBlocked())
			return;
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
	public void deserializeNBT(NBTBase nbt) {
		if (!init)
			setEventBlock(true);
		super.deserializeNBT(nbt);
		int size = AlchemyBaubles.getBaublesSize();
		if (contents.length < size)
			contents = ArrayUtils.addAll(contents, new ItemStack[size - contents.length]);
		update(true);
		if (!init) {
			setEventBlock(false);
			init = true;
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player) {
		return stack != null && stack.getItem() instanceof IBauble &&
				((IBauble) stack.getItem()).canEquip(stack, player) &&
				((IBauble) stack.getItem()).getBaubleType(stack).hasSlot(slot);
	}

	@Override
	public boolean isEventBlocked() {
		return blockEvents;
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	public boolean isChanged(int slot) {
		return changed[slot];
	}

	@Override
	public void setChanged(int slot, boolean changed) {
		this.changed[slot] = changed;
		markDirty();
	}

}
