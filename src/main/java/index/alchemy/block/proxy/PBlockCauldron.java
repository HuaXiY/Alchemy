package index.alchemy.block.proxy;

import java.util.LinkedList;

import javax.annotation.Nullable;

import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.IRegister;
import index.alchemy.api.ITileEntity;
import index.alchemy.api.annotation.Change;
import index.alchemy.api.annotation.Proxy;
import index.alchemy.api.event.CauldronActivatedEvent;
import index.alchemy.tile.TileEntityCauldron;
import index.alchemy.util.Always;
import index.alchemy.util.InventoryHelper;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

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
		int i = ((Integer)state.getValue(LEVEL)).intValue();
		
		TileEntityCauldron cauldron = getTileEntityCauldron(world, pos);
		
		CauldronActivatedEvent event;
		if (MinecraftForge.EVENT_BUS.post(event = new CauldronActivatedEvent(world, pos, state, player, hand, cauldron,
				heldItem, side, hitX, hitY, hitZ)))
			return event.getResult() == Result.DEFAULT;
		
		if (heldItem == null) {
			if (Always.isServer()) {
				LinkedList<ItemStack> list = getTileEntityCauldron(world, pos).getContainer();
				if (list.size() > 0) {
					player.setHeldItem(hand, list.removeLast());
					player.inventory.markDirty();
					cauldron.updateTracker();
				}
			}
		} else {
			Item item = heldItem.getItem();
			if (item instanceof ItemArmor) {
				if (i > 0) {
					ItemArmor armor = (ItemArmor)item;
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
			} else {
				FluidUtil.interactWithFluidHandler(heldItem, cauldron.getTank(), player);
			}
		}
		return true;
	}
	
	@Nullable
	public TileEntityCauldron getTileEntityCauldron(World world, BlockPos pos) {
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
		IBlockState liquid = ((TileEntityCauldron) world.getTileEntity(pos)).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material material) {
		IBlockState liquid = ((TileEntityCauldron) world.getTileEntity(pos)).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public boolean isMaterialInBB(World world, BlockPos pos, Material material) {
		IBlockState liquid = ((TileEntityCauldron) world.getTileEntity(pos)).getLiquid();
		return liquid == null ? false : liquid.getMaterial() == material;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityCauldron te = ((TileEntityCauldron) world.getTileEntity(pos));
		IBlockState liquid = te == null ? null : te.getLiquid();
		return liquid == null ? super.getLightValue(state, world, pos) : liquid.getLightValue();
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