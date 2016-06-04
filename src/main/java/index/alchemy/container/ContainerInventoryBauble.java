package index.alchemy.container;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.annotation.Proxy;
import index.alchemy.annotation.Texture;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.inventory.InventoryBauble;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@Proxy(ContainerPlayer.class)
public class ContainerInventoryBauble extends ContainerPlayer {
	
	public static final String EMPTY_NAME[] = SlotBauble.class.getAnnotation(Texture.class).value();
	
	@Texture({
		"alchemy:items/empty_ring",
		"alchemy:items/empty_amulet",
		"alchemy:items/empty_belt"
	})
	private class SlotBauble extends Slot {
		
		private BaubleType type;

		public SlotBauble(IInventory inventory, BaubleType type, int index, int x, int y) {
			super(inventory, index, x, y);
			this.type = type;
			setBackgroundName(EMPTY_NAME[type.ordinal()]);
		}
		
		public BaubleType getType() {
			return type;
		}
		
		@Override
		public boolean isItemValid(ItemStack item) {
			return item!=null && item.getItem() !=null &&
					item.getItem() instanceof IBauble && 
				   ((IBauble) item.getItem()).getBaubleType(item)== type &&
				   ((IBauble) item.getItem()).canEquip(item, baubles.getLiving());
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return this.getStack()!=null &&
				   ((IBauble) getStack().getItem()).canUnequip(getStack(), player);
		}

		@Override
		public int getSlotStackLimit() {
			return 1;
		}
		
	}
	
	private InventoryBauble baubles;
	private int start_id;

	public ContainerInventoryBauble(InventoryPlayer playerventory, boolean localWorld, EntityPlayer player) {
		super(playerventory, localWorld, player);
		
		Slot shield = inventorySlots.get(inventorySlots.size() - 1);
		shield.xDisplayPosition += 20;
		
		baubles = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		start_id = inventorySlots.size();
		addSlotToContainer(new SlotBauble(baubles, BaubleType.AMULET, 0, 77, 8 ));
		addSlotToContainer(new SlotBauble(baubles, BaubleType.RING, 1, 77, 8 + 1 * 18));
		addSlotToContainer(new SlotBauble(baubles, BaubleType.RING, 2, 77, 8 + 2 * 18));
		addSlotToContainer(new SlotBauble(baubles, BaubleType.BELT, 3, 77, 8 + 3 * 18));
		
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		baubles.closeInventory(player);
		super.onContainerClosed(player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack item = null;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack slot_item = slot.getStack();
			item = slot_item.copy();
			EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(item);

			if (index == 0) { // craft output
				if (!mergeItemStack(slot_item, 9, 45, true))
					return null;
				slot.onSlotChange(slot_item, item);
			} else if (index >= 1 && index < 5) { // craft input
				if (!mergeItemStack(slot_item, 9, 45, false))
					return null;
			}  else if (index >= 5 && index < 9) { // equipment
				if (!mergeItemStack(slot_item, 9, 45, false))
					return null;
			} else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !inventorySlots.get(8 - entityequipmentslot.getIndex()).getHasStack()) {
				int i = 8 - entityequipmentslot.getIndex();
				if (!mergeItemStack(slot_item, i, i + 1, false))
					return null;
			} else if (item.getItem() instanceof IBauble) {
				IBauble bauble = (IBauble) item.getItem();
				if (!mergeItemStack(slot_item, start_id, start_id + 4, false))
					return null;
			} else if (index >= 9 && index < 36) { // backpack
				if (!mergeItemStack(slot_item, 36, 45, false))
					return null;
			}
			else if (index >= 36 && index < 45) { // shortcut bar
				if (!mergeItemStack(slot_item, 9, 36, false))
					return null;
			} else if (!mergeItemStack(slot_item, 9, 45, false)) {
				return null;
			}

			if (slot_item.stackSize == 0)
				slot.putStack((ItemStack)null);
			else
				slot.onSlotChanged();

			if (slot_item.stackSize == item.stackSize)
				return null;

			slot.onPickupFromSlot(player, slot_item);
		}
		return item;
	}

}
