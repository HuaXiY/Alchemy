package index.alchemy.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import index.alchemy.api.ILocationProvider;
import index.alchemy.api.IMaterialConsumer;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.util.cache.ThreadContextCache;
import index.project.version.annotation.Beta;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;
import net.minecraftforge.oredict.OreDictionary;

import static java.lang.Math.*;

import java.io.File;

@Omega
public class Always {
	
	public static int maxHeight = 256;
	
	private static boolean isClient = $.forName("net.minecraft.client.Minecraft", false) != null;
	
	public static final ThreadContextCache<Side> SIDE_CONTEXT = new ThreadContextCache<Side>().setOnMissGet(FMLCommonHandler.instance()::getEffectiveSide);
	
	public static final boolean isAlchemyModLoaded() {
		return Loader.isModLoaded(AlchemyConstants.MOD_ID);
	}
	
	@SideOnly(Side.CLIENT)
	public static final EntityPlayer lookupPlayer() {
		return Minecraft.getMinecraft().player;
	}
	
	@SideOnly(Side.CLIENT)
	public static final World lookupWorld() {
		return Minecraft.getMinecraft().world;
	}
	
	@SideOnly(Side.CLIENT)
	public static final boolean isPlaying() {
		return Minecraft.getMinecraft().player != null;
	}
	
	@SideOnly(Side.CLIENT)
	public static final long getClientWorldTime() {
		return Minecraft.getMinecraft().world.getWorldTime();
	}
	
	public static final boolean runOnClient() {
		return isClient;
	}
	
	public static final void markSide(Side side) {
		markSide(Thread.currentThread(), side);
	}
	
	public static final void markSide(Thread target, Side side) {
		SIDE_CONTEXT.add(target, side);
	}
	
	public static final Side getSide() {
		return SIDE_CONTEXT.get();
	}
	
	@Nullable
	public static final UUID getUUIDFromPlayerName(String name) {
		return UsernameCache.getMap()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))
				.get(name);
	}
	
	@Nullable
	public static final File getWorldDirectory() {
		if (DimensionManager.getWorld(0) != null)
			return DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory();
		else if (FMLServerHandler.instance().getServer() != null) {
			MinecraftServer server = FMLServerHandler.instance().getServer();
			return server.getActiveAnvilConverter().getSaveLoader(server.getFolderName(), false).getWorldDirectory();
		} else
			return null;
	}
	
	public static final boolean isServer() {
		return getSide().isServer();
	}
	
	public static final boolean isClient() {
		return getSide().isClient();
	}
	
	public static final void changeHeldItemIndex(EntityPlayer player, int index) {
		if (Always.isClient())
			player.inventory.currentItem = index;
		else if (player instanceof EntityPlayerMP)
			((EntityPlayerMP) player).connection.sendPacket(new SPacketHeldItemChange(index));
	}
	
	public static final ItemStack getEnchantmentBook(Enchantment enchantment) {
		return getEnchantmentBook(enchantment, 0);
	}
	
	public static final ItemStack getEnchantmentBook(Enchantment enchantment, int level) {
		return ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, level));
	}
	
	public static final double calculateTheStraightLineDistance(double x, double y, double z) {
		return x * x + y * y + z * z;
	}
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public static final Entity findEntityFormClientWorld(int id) {
		World world = Minecraft.getMinecraft().world;
		if (world != null)
			return world.getEntityByID(id);
		return null;
	}
	
	public static final Biome getCurrentBiome(EntityPlayer player) {
		return getCurrentBiome(player.world, (int) player.posX, (int) player.posZ);
	}
	
	public static final Biome getCurrentBiome(World world, int x, int z) {
		return world.getBiomeForCoordsBody(new BlockPos(x, 0, z));
	}
	
	public static final ILocationProvider generateLocationProvider(Entity entity, double offsetY) {
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				AxisAlignedBB aabb = entity.getEntityBoundingBox();
				return entity.getPositionVector()
						.addVector((aabb.maxX - aabb.minX) / 2, offsetY * (aabb.maxY - aabb.minY) / 2, (aabb.maxZ - aabb.minZ) / 2);
			}
		};
	}
	
	public static final ILocationProvider generateLocationProvider(BlockPos pos) {
		Vec3d vec3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				return vec3d;
			}
		};
	}
	
	@Beta
	public static final List<IMaterialConsumer> generateMaterialConsumers(Object... args) {
		Tool.checkNull(args);
		List<IMaterialConsumer> result = Lists.newLinkedList();
		for (int i = 0, len = args.length + 1; i < len; i++) {
			Object last = i > 0 ? args[i - 1] : null, obj = i == args.length ? null : args[i];
			if (last != null && !(last instanceof ItemStack || last instanceof Number)) {
				if (last instanceof Item)
					result.add(generateMaterialConsumer(new ItemStack((Item) last, obj instanceof Number ?
							((Number) obj).intValue() : 1)));
				else if (last instanceof Block)
					result.add(generateMaterialConsumer(new ItemStack((Block) last, obj instanceof Number ?
							((Number) obj).intValue() : 1)));
				else if (last instanceof String)
					result.add(generateMaterialConsumer((String) last, obj instanceof Number ?
							((Number) obj).intValue() : 1));
				else
					throw new IllegalArgumentException("Type mismatch, type: " + last.getClass().getName() + " , index: " + (i - 1));
			}
			if (obj != null && obj instanceof ItemStack)
				result.add(generateMaterialConsumer((ItemStack) obj));
		}
		return result;
	}
	
	public static final IMaterialConsumer generateMaterialConsumer(ItemStack material) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				int need = material.getCount();
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					if (item.isItemEqualIgnoreDurability(material)) {
						int change = min(need, item.getCount());
						need -= change;
						item.setCount(item.getCount() - change);
						if (item.getCount() == 0)
							iterator.remove();
						if (need < 1)
							return true;
					}
				}
				return false;
			}
		};
	}
	
	public static final IMaterialConsumer generateMaterialConsumer(String material_str, int size) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				int need = size;
				NonNullList<ItemStack> ods = OreDictionary.getOres(material_str);
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					for (ItemStack material : ods)
						if (item.isItemEqualIgnoreDurability(material)) {
							int change = min(need, item.getCount());
							need -= change;
							item.setCount(item.getCount() - change);
							if (item.getCount() == 0)
								iterator.remove();
							if (need < 1)
								return true;
							break;
						}
				}
				return false;
			}
		};
	}
	
}