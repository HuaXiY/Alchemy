package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.IAlchemyRecipe;
import index.alchemy.api.IBaubleEquipment;
import index.alchemy.api.IMaterialConsumer;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class AlchemyItemBauble extends AlchemyItemColor implements IBauble, IBaubleEquipment, IAlchemyRecipe {
	
	protected int alchemyTime = 20 * 30, alchemyColor = -1;
	protected final List<IMaterialConsumer> alchemyMaterials = new LinkedList<>();
	
	public void setAlchemyTime(int alchemyTime) {
		this.alchemyTime = alchemyTime;
	}
	
	public void setAlchemyColor(int alchemyColor) {
		this.alchemyColor = alchemyColor;
	}
	
	public static class AlchemyItemAmulet extends AlchemyItemBauble {
		
		@Override
		public BaubleType getBaubleType(ItemStack itemstack) {
			return BaubleType.AMULET;
		}
		
		@Override
		public boolean canEquip(ItemStack item, EntityLivingBase player) {
			return player.getCapability(AlchemyCapabilityLoader.bauble, null).getStackInSlot(0) == null;
		}
		
		@Override
		public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
			if (Always.isServer() && canEquip(item, player)) {
				IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
				if (inventory.getStackInSlot(0) == null) {
					inventory.setInventorySlotContents(0, item.copy());
					item.stackSize--;
					return new ActionResult(EnumActionResult.SUCCESS, item);
				}
			}
	        return new ActionResult(EnumActionResult.PASS, item);
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
			IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
			return isOnly() ?
				inventory.getStackInSlot(2) == null && (inventory.getStackInSlot(1) == null || inventory.getStackInSlot(1).getItem() != this) ||
				inventory.getStackInSlot(1) == null && (inventory.getStackInSlot(2) == null || inventory.getStackInSlot(2).getItem() != this) : 
				inventory.getStackInSlot(1) == null || inventory.getStackInSlot(2) == null;
		}
		
		@Override
		public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
			if (Always.isServer() && canEquip(item, player)) {
				IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
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
			return player.getCapability(AlchemyCapabilityLoader.bauble, null).getStackInSlot(3) == null;
		}
		
		@Override
		public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
			if (Always.isServer() && canEquip(item, player)) {
				IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
				if (inventory.getStackInSlot(3) == null) {
					inventory.setInventorySlotContents(3, item.copy());
					item.stackSize--;
					return new ActionResult(EnumActionResult.SUCCESS, item);
				}
			}
	        return new ActionResult(EnumActionResult.PASS, item);
	    }
		
		public AlchemyItemBelt(String name, int color) {
			super(name, "belt", color);
		}
		
	}

	@Nullable
	@Override
	public ItemStack getFormLiving(EntityLivingBase living) {
		InventoryBauble inventory = living.getCapability(AlchemyCapabilityLoader.bauble, null);
		if (inventory != null)
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null && item.getItem() == this)
					return item;
			}
		return null;
	}
	
	@Override
	public ResourceLocation getAlchemyName() {
		return getRegistryName();
	}
	
	@Override
	public int getAlchemyTime() {
		return alchemyTime;
	}
	
	@Override
	public int getAlchemyColor() {
		return alchemyColor == -1 ? color : alchemyColor;
	}
	
	@Override
	public ItemStack getAlchemyResult() {
		return new ItemStack(this);
	}
	
	@Override
	public List<IMaterialConsumer> getAlchemyMaterials() {
		return alchemyMaterials;
	}
	
	public AlchemyItemBauble(String name, String icon_name, int color) {
		super(name, icon_name, color);
		setMaxStackSize(1);
	}
	
	public static ItemStack setBauble(EntityPlayer player, int index, ItemStack item) {
		IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		ItemStack result = inventory.getStackInSlot(index);
		inventory.setInventorySlotContents(index, item);
		return result;
	}
	
	@Nullable
	public static ItemStack getBauble(EntityLivingBase living, int index) {
		InventoryBauble inventory = living.getCapability(AlchemyCapabilityLoader.bauble, null);
		return inventory == null ? null : inventory.getStackInSlot(index);
	}

}
