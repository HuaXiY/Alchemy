package index.alchemy.util;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryHelper  {
	
	private static final Random RANDOM = new Random();

	public static final void dropInventoryItems(World world, BlockPos pos, IInventory inventory) {
		dropInventoryItems(world, pos.getX(), pos.getY(), pos.getZ(), inventory);
	}

	public static final void dropInventoryItems(World world, Entity entity, IInventory inventory) {
		dropInventoryItems(world, entity.posX, entity.posY, entity.posZ, inventory);
	}

	private static void dropInventoryItems(World world, double x, double y, double z, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); ++i)  {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null)
				spawnItemStack(world, x, y, z, item);
		}
	}
	
	public static final void dropInventoryItems(World world, BlockPos pos, List<ItemStack> inventory) {
		dropInventoryItems(world, pos.getX(), pos.getY(), pos.getZ(), inventory);
	}

	public static final void dropInventoryItems(World world, Entity entity, List<ItemStack> inventory) {
		dropInventoryItems(world, entity.posX, entity.posY, entity.posZ, inventory);
	}

	private static void dropInventoryItems(World world, double x, double y, double z, List<ItemStack> inventory) {
		for (ItemStack item : inventory)
			if (item != null)
				spawnItemStack(world, x, y, z, item);
	}

	public static final void spawnItemStack(World world, double x, double y, double z, ItemStack item) {
		float mx = RANDOM.nextFloat() * 0.8F + 0.1F;
		float my = RANDOM.nextFloat() * 0.8F + 0.1F;
		float mz = RANDOM.nextFloat() * 0.8F + 0.1F;

		while (item.stackSize > 0) {
			int i = Math.min(RANDOM.nextInt(21) + 10, item.stackSize);

			item.stackSize -= i;
			EntityItem entityitem = new EntityItem(world, x + mx, y + my, z + mz, item.splitStack(i));
			
			float f = 0.05F;
			entityitem.motionX = RANDOM.nextGaussian() * f;
			entityitem.motionY = RANDOM.nextGaussian() * f + 0.2D;
			entityitem.motionZ = RANDOM.nextGaussian() * f;
			world.spawnEntityInWorld(entityitem);
		}
	}
	
	public static final EntityItem getEntityItem(Entity entity, ItemStack item) {
		return getEntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, item);
	}
	
	public static final EntityItem getEntityItem(World world, double x, double y, double z, ItemStack item) {
		float mx = RANDOM.nextFloat() * 0.8F + 0.1F;
		float my = RANDOM.nextFloat() * 0.8F + 0.1F;
		float mz = RANDOM.nextFloat() * 0.8F + 0.1F;
		
		EntityItem entityitem = new EntityItem(world, x + mx, y + my, z + mz, item);
		
		float f = 0.05F;
		entityitem.motionX = RANDOM.nextGaussian() * f;
		entityitem.motionY = RANDOM.nextGaussian() * f + 0.2D;
		entityitem.motionZ = RANDOM.nextGaussian() * f;
		
		return entityitem;
	}
	
	public static final void addItemStackOrSetToHand(EntityPlayer player, EnumHand hand, ItemStack heldItem, ItemStack item) {
		if (!player.capabilities.isCreativeMode && --heldItem.stackSize == 0)
			player.setHeldItem(hand, item);
		else if (item != null && !player.inventory.addItemStackToInventory(item))
			player.dropItem(item, false);
	}
	
	public static final void addNonCreativeModeItemStackOrSetToHand(EntityPlayer player, EnumHand hand, ItemStack heldItem, ItemStack item) {
		if (!player.capabilities.isCreativeMode)
			if (--heldItem.stackSize == 0)
				player.setHeldItem(hand, item);
			else if (item != null && !player.inventory.addItemStackToInventory(item))
				player.dropItem(item, false);
	}
	
	public static final boolean canMergeItemStack(@Nonnull ItemStack a, @Nonnull ItemStack b) {
		return 	a.getItem() == b.getItem() &&
				a.getItemDamage() == b.getItemDamage() &&
				a.getMetadata() == b.getMetadata() &&
				Objects.equals(a.getTagCompound(), b.getTagCompound()) &&
				a.areCapsCompatible(b);
	}
	
	public static final boolean areItemsEqual(ItemStack a, ItemStack b) {
		return a == null ? b == null : b != null && canMergeItemStack(a, b);
	}
	
	public static final boolean areItemsMetaEqual(ItemStack a, ItemStack b) {
		return a == null ? b == null : b != null && a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
	}
	
	public static final boolean isItem(ItemStack stack, Item item) {
		return stack != null && stack.getItem() == item;
	}
	
}