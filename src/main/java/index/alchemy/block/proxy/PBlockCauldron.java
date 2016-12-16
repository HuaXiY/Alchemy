package index.alchemy.block.proxy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Nullable;

import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.IRegister;
import index.alchemy.api.ITileEntity;
import index.alchemy.api.annotation.Change;
import index.alchemy.api.annotation.Proxy;
import index.alchemy.api.event.CauldronActivatedEvent;
import index.alchemy.interacting.ModItems;
import index.alchemy.tile.TileEntityCauldron;
import index.alchemy.util.Always;
import index.alchemy.util.InventoryHelper;
import index.project.version.annotation.Beta;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.rod.ItemWaterRod;

@Beta
@Change("1.9.4")
@Proxy("net.minecraft.block.BlockCauldron")
public class PBlockCauldron extends BlockCauldron implements ITileEntity, IMaterialContainer, IRegister {
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCauldron();
	}

	@Override
	public Class<? extends TileEntity> getTileEntityClass() {
		return TileEntityCauldron.class;
	}

	@Override
	public String getTileEntityName() {
		return getUnlocalizedName();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		getTileEntityCauldron(world, pos).onBlockBreak();
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			@Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityCauldron cauldron = getTileEntityCauldron(world, pos);
		
		CauldronActivatedEvent event;
		if (MinecraftForge.EVENT_BUS.post(event = new CauldronActivatedEvent(world, pos, state, player, hand, cauldron,
				heldItem, side, hitX, hitY, hitZ)))
			return event.getResult() == Result.DEFAULT;
		
		LinkedList<ItemStack> list = getTileEntityCauldron(world, pos).getContainer();
		if (heldItem == null) {
			if (Always.isServer()) {
				if (list.size() > 0) {
					player.setHeldItem(hand, list.removeLast());
					player.inventory.markDirty();
					cauldron.onContainerChange();
					cauldron.updateTracker();
				}
			}
		} else {
			int i = getWaterLevel(world, pos, state);
			Item item = heldItem.getItem();
			if (item == ModItems.botania$waterRod) {
				if (i > -1 && i < 3 && ManaItemHandler.requestManaExact(heldItem, player, ItemWaterRod.COST, true))
					setWaterLevel(world, pos, state, 3);
			} else if (FluidUtil.getFluidHandler(heldItem) != null) {
				FluidUtil.interactWithFluidHandler(heldItem, cauldron.getTank(), player);
			} else if (!list.isEmpty()) {
				if (Always.isServer()) {
					boolean flag = false;
					int limit = heldItem.getMaxStackSize();
					if (heldItem.stackSize >= limit)
						return false;
					for (Iterator<ItemStack> iterator = list.iterator(); iterator.hasNext();) {
						ItemStack citem = iterator.next();
						if (InventoryHelper.canMergeItemStack(heldItem, citem))
							if (heldItem.stackSize < limit) {
								heldItem.stackSize++;
								iterator.remove();
								flag = true;
							} else
								break;
					}
					if (flag) {
						player.inventory.markDirty();
						cauldron.onContainerChange();
						cauldron.updateTracker();
					}
				}
			} else {
				if (item instanceof ItemArmor) {
					if (i > 0) {
						ItemArmor armor = (ItemArmor) item;
						if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && armor.hasColor(heldItem)) {
							armor.removeColor(heldItem);
							player.addStat(StatList.ARMOR_CLEANED);
							setWaterLevel(world, pos, state, i - 1);
						}
					}
				} else if (item instanceof ItemBanner) {
					if (i > 0 && TileEntityBanner.getPatterns(heldItem) > 0) {
						ItemStack banner = heldItem.copy();
						banner.stackSize = 1;
						TileEntityBanner.removeBannerData(banner);
						InventoryHelper.addItemStackOrSetToHand(player, hand, heldItem, banner);
						player.addStat(StatList.BANNER_CLEANED);
						setWaterLevel(world, pos, state, i - 1);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		IBlockState liquid = getTileEntityCauldron(world, pos).getLiquid();
		if (liquid != null)
			liquid.getBlock().randomDisplayTick(liquid, world, pos, rand);
	}
	
	@Nullable
	public TileEntityCauldron getTileEntityCauldron(IBlockAccess world, BlockPos pos) {
		return (TileEntityCauldron) world.getTileEntity(pos);
	}
	
	@Override
	public void setWaterLevel(World world, BlockPos pos, IBlockState state, int level) {
		getTileEntityCauldron(world, pos).setLevel(level);
	}
	
	public int getWaterLevel(World world, BlockPos pos, IBlockState state) {
		return getTileEntityCauldron(world, pos).getLevel();
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		IBlockState liquid = getTileEntityCauldron(world, pos).getLiquid();
		if (liquid != null) {
			liquid.getBlock().onEntityWalk(world, pos, entity);
			liquid.getBlock().onEntityCollidedWithBlock(world, pos, state, entity);
		}
		super.onEntityCollidedWithBlock(world, pos, state, entity);
	}
	
	@Override
	public void fillWithRain(World world, BlockPos pos) {
		if (world.rand.nextInt(20) == 1) {
			float f = world.getBiome(pos).getFloatTemperature(pos);
			if (world.getBiomeProvider().getTemperatureAtHeight(f, pos.getY()) >= 0.15F) {
				int level = getWaterLevel(world, pos, null);
				if (level < 3 && level > -1)
					setWaterLevel(world, pos, null, level + 1);
			}
		}
	}

	@Override
	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity,
			double yToTest, Material material, boolean testingHead) {
		IBlockState liquid = getTileEntityCauldron(world, pos).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material material) {
		IBlockState liquid = getTileEntityCauldron(world, pos).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public boolean isMaterialInBB(World world, BlockPos pos, Material material) {
		IBlockState liquid = getTileEntityCauldron(world, pos).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (world.getTileEntity(pos) instanceof TileEntityCauldron) {
			TileEntityCauldron te = getTileEntityCauldron(world, pos);
			IBlockState liquid = te == null ? null : te.getLiquid();
			return liquid == null ? super.getLightValue(state, world, pos) : liquid.getLightValue();
		} return super.getLightValue(state, world, pos);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		return getTileEntityCauldron(world, pos).getLevel();
	}
	
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? super.eventReceived(state, worldIn, pos, id, param) : tileentity.receiveClientEvent(id, param);
	}
	
	public PBlockCauldron() {
		setHardness(2.0F);
		setUnlocalizedName("cauldron");
		register();
	}
	
	@Override
	public boolean shouldRegisterToGame() {
		return false;
	}
	
}