package index.alchemy.item;

import index.alchemy.api.Alway;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.common.lib.PlayerHandler;

public abstract class AlchemyItemBauble extends AlchemyItemColor implements IBauble, IBaubleEquipment {
	

	public static class AlchemyItemAmulet extends AlchemyItemBauble {
		
		@Override
		public BaubleType getBaubleType(ItemStack itemstack) {
			return BaubleType.AMULET;
		}
		
		@Override
		public boolean canEquip(ItemStack item, EntityLivingBase player) {
			return PlayerHandler.getPlayerBaubles((EntityPlayer) player).getStackInSlot(0) == null;
		}
		
		public AlchemyItemAmulet(String name, int color) {
			super(name, "amulet", color);
		}
		
	}
	
	public static class AlchemyItemRing extends AlchemyItemBauble {
		
		@Override
		public BaubleType getBaubleType(ItemStack itemstack) {
			return BaubleType.RING;
		}
		
		@Override
		public boolean canEquip(ItemStack item, EntityLivingBase player) {
			IInventory inventory = PlayerHandler.getPlayerBaubles((EntityPlayer) player);
			return isOnly() ? inventory.getStackInSlot(1) == null || inventory.getStackInSlot(2) == null :
				inventory.getStackInSlot(1) == null || inventory.getStackInSlot(1).getItem() != this && inventory.getStackInSlot(2) == null;
		}
		
		@Override
		public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
			if (Alway.isServer() && canEquip(item, player)) {
				IInventory inventory = PlayerHandler.getPlayerBaubles((EntityPlayer) player);
				for (int i = 1; i < 3; i++)
					if (inventory.getStackInSlot(i) == null) {
						inventory.setInventorySlotContents(i, item.copy());
						item.stackSize--;
						return new ActionResult(EnumActionResult.SUCCESS, item);
					}
			}
	        return new ActionResult(EnumActionResult.PASS, item);
	    }
		
		public AlchemyItemRing(String name, int color) {
			super(name, "ring", color);
		}
		
		public boolean isOnly() {
			return true;
		}
		
	}
	
	public static class AlchemyItemBelt extends AlchemyItemBauble {
		
		@Override
		public BaubleType getBaubleType(ItemStack itemstack) {
			return BaubleType.BELT;
		}
		
		@Override
		public boolean canEquip(ItemStack item, EntityLivingBase player) {
			return PlayerHandler.getPlayerBaubles((EntityPlayer) player).getStackInSlot(3) == null;
		}
		
		
		public AlchemyItemBelt(String name, int color) {
			super(name, "belt", color);
		}
		
	}

	@Override
	public BaubleType getBaubleType(ItemStack item) {
		return null;
	}

	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {}

	@Override
	public void onEquipped(ItemStack item, EntityLivingBase living) {}

	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {}

	@Override
	public abstract boolean canEquip(ItemStack item, EntityLivingBase living);

	@Override
	public boolean canUnequip(ItemStack item, EntityLivingBase living) {
		return true;
	}
	
	@Override
	public boolean isEquipmented(EntityPlayer player) {
		IInventory inventory = PlayerHandler.getPlayerBaubles(player);
		for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null && item.getItem() == this)
				return true;
		}
		return false;
	}
	
	@Override
	public ItemStack getFormPlayer(EntityPlayer player) {
		IInventory inventory = PlayerHandler.getPlayerBaubles(player);
		for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null && item.getItem() == this)
				return item;
		}
		return null;
	}
	
	public AlchemyItemBauble(String name, String icon_name, int color) {
		super(name, icon_name, color);
		setMaxStackSize(1);
	}

}
