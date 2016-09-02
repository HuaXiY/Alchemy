package index.alchemy.inventory;

import baubles.api.IBauble;
import index.alchemy.api.Always;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageNBTUpdate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class InventoryBauble extends AlchemyInventory {
	
	public static final int SIZE = 4, LIMIT = 1;
	public static final String NAME = "bauble";
	
	protected EntityLivingBase living;
	
	public InventoryBauble(final EntityLivingBase living) {
		super(SIZE, NAME, LIMIT);
		this.living = living;
	}
	
	@Override
	public void markDirty() {}
	
	public void update(boolean init) {
		if (Always.isServer() && (!init || hasItem()))
			updateTracker();
	}
	
	public void updateTracker() {
		final NBTTagCompound data = serializeNBT();
		AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
			@Override
			public void run(Phase phase) {
				for (EntityPlayer player : ((WorldServer) living.worldObj).getEntityTracker().getTrackingPlayers(living))
					updatePlayer((EntityPlayerMP) player, data);
				if (living instanceof EntityPlayerMP)
					updatePlayer((EntityPlayerMP) living, data);
			}
		}, 1);
	}
	
	public void updatePlayer(EntityPlayerMP player) {
		updatePlayer(player, serializeNBT());
	}
	
	public void updatePlayer(EntityPlayerMP player, NBTTagCompound data) {
		AlchemyNetworkHandler.updateEntityNBT(MessageNBTUpdate.Type.ENTITY_BAUBLE_DATA, living.getEntityId(), data, player);
	}
	
	public void copy(EntityLivingBase living) {
		living.getCapability(AlchemyCapabilityLoader.bauble, null).deserializeNBT(serializeNBT());
	}
	
	public EntityLivingBase getLiving() {
		return living;
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack item = super.decrStackSize(index, count);
		if (item != null) {
			change(item, null);
			update(false);
		}
		return item;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack item = super.removeStackFromSlot(index);
		if (item != null) {
			change(item, null);
			update(false);
		}
		return item;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack item) {
		ItemStack old = contents[index];
		super.setInventorySlotContents(index, item);
		if (old != item) {
			change(old, item);
			update(false);
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
	public void deserializeNBT(NBTBase nbt) {
		super.deserializeNBT(nbt);
		update(true);
	}

}
