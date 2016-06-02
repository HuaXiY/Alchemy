package index.alchemy.util;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryHelper  {
	
	private static final Random RANDOM = new Random();

	public static void dropInventoryItems(World world, BlockPos pos, IInventory inventory) {
		dropInventoryItems(world, pos.getX(), pos.getY(), pos.getZ(), inventory);
	}

	public static void dropInventoryItems(World world, Entity entity, IInventory inventory) {
		dropInventoryItems(world, entity.posX, entity.posY, entity.posZ, inventory);
	}

	private static void dropInventoryItems(World world, double x, double y, double z, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); ++i)  {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null)
				spawnItemStack(world, x, y, z, item);
		}
	}
	
	public static void dropInventoryItems(World world, BlockPos pos, List<ItemStack> inventory) {
		dropInventoryItems(world, pos.getX(), pos.getY(), pos.getZ(), inventory);
	}

	public static void dropInventoryItems(World world, Entity entity, List<ItemStack> inventory) {
		dropInventoryItems(world, entity.posX, entity.posY, entity.posZ, inventory);
	}

	private static void dropInventoryItems(World world, double x, double y, double z, List<ItemStack> inventory) {
		for (ItemStack item : inventory)
			if (item != null)
				spawnItemStack(world, x, y, z, item);
	}

	public static void spawnItemStack(World world, double x, double y, double z, ItemStack item) {
		float mx = RANDOM.nextFloat() * 0.8F + 0.1F;
		float my = RANDOM.nextFloat() * 0.8F + 0.1F;
		float mz = RANDOM.nextFloat() * 0.8F + 0.1F;

		while (item.stackSize > 0) {
			int i = Math.min(RANDOM.nextInt(21) + 10, item.stackSize);

			item.stackSize -= i;
			EntityItem entityitem = new EntityItem(world, x + mx, y + my, z + mz, new ItemStack(item.getItem(), i, item.getMetadata()));

			if (item.hasTagCompound())
				entityitem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());

			float f = 0.05F;
			entityitem.motionX = RANDOM.nextGaussian() * f;
			entityitem.motionY = RANDOM.nextGaussian() * f + 0.2D;
			entityitem.motionZ = RANDOM.nextGaussian() * f;
			world.spawnEntityInWorld(entityitem);
		}
	}
	
	public static EntityItem getEntityItem(Entity entity, ItemStack item) {
		return getEntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, item);
	}
	
	public static EntityItem getEntityItem(World world, double x, double y, double z, ItemStack item) {
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
	
	public static void addItemStackOrSetToHand(EntityPlayer player, EnumHand hand, ItemStack heldItem, ItemStack item) {
		if (!player.capabilities.isCreativeMode && --heldItem.stackSize == 0)
			player.setHeldItem(hand, item);
		else if (item != null && !player.inventory.addItemStackToInventory(item))
			player.dropItem(item, false);
	}
	
}