package index.alchemy.dlcs.skin.core;

import javax.annotation.Nullable;

import index.alchemy.dlcs.skin.core.BlockWardrobe.EnumPartType;
import index.alchemy.tile.AlchemyTileEntity;
import index.alchemy.util.Always;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TileWardrobe extends AlchemyTileEntity implements ITickable {
	
	public static final String NBT_KEY_LINK_FACING = "link_facing", NBT_KEY_RELY_TYPE = "rely_type";
	
	@Nullable
	protected EnumFacing linkFacing;
	
	@Nullable
	protected BlockWardrobe.EnumRelyType relyType;
	
	public EnumFacing getLinkFacing() {
		return linkFacing;
	}
	
	public void setLinkFacing(EnumFacing linkFacing) {
		this.linkFacing = linkFacing;
	}
	
	public BlockWardrobe.EnumRelyType getRelyType() {
		return relyType;
	}
	
	public void setRelyType(BlockWardrobe.EnumRelyType relyType) {
		this.relyType = relyType;
	}
	
	public boolean shouldLinkFacing() {
		return getLinkFacing() == null || getRelyType() == null;
	}
	
	public static BlockPos updateFacing(World world, BlockPos pos, IBlockState state) {
		TileWardrobe wardrobe = (TileWardrobe) world.getTileEntity(pos);
		EnumFacing facing = state.getValue(BlockWardrobe.FACING), temp = facing.rotateY();
		if (!tryUpdate(world, pos, state.getBlock(), temp, facing, BlockWardrobe.EnumRelyType.LEFT))
			if (!tryUpdate(world, pos, state.getBlock(), temp = temp.getOpposite(), facing, BlockWardrobe.EnumRelyType.RIGHT))
				return null;
			else {
				wardrobe.setLinkFacing(temp);
				wardrobe.setRelyType(BlockWardrobe.EnumRelyType.LEFT);
			}
		else {
			wardrobe.setLinkFacing(temp);
			wardrobe.setRelyType(BlockWardrobe.EnumRelyType.RIGHT);
		}
		return pos.offset(temp);
	}
	
	public static boolean tryUpdate(World world, BlockPos pos, Block blcok, EnumFacing linkFacing,
			EnumFacing srcFacing, BlockWardrobe.EnumRelyType type) {
		pos = pos.offset(linkFacing);
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == blcok && state.getValue(BlockWardrobe.FACING) == srcFacing) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileWardrobe) {
				TileWardrobe wardrobe = (TileWardrobe) tile;
				if (wardrobe.shouldLinkFacing()) {
					wardrobe.setLinkFacing(linkFacing.getOpposite());
					wardrobe.setRelyType(type);
					return true;
				}
			}
		}
		return false;
	}
	
	public static IBlockState checkState(IBlockAccess world, BlockPos pos, IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileWardrobe) {
			TileWardrobe wardrobe = (TileWardrobe) tile;
			return state.withProperty(BlockWardrobe.RELY, wardrobe.getRelyType());
		}
		return state;
	}
	
	public boolean checkState(World world, Block block, BlockPos posA, IBlockState stateA, BlockPos posB, IBlockState stateB) {
		if (stateA.getBlock() != block || stateB.getBlock() != block)
			return false;
		updateState(world, posA, stateA);
		updateState(world, posB, stateB);
		world.markBlockRangeForRenderUpdate(posA, posB);
		world.markBlockRangeForRenderUpdate(posA.up(), posB.up());
		return true;
	}
	
	public void updateState(World world, BlockPos pos, IBlockState state) {
		IBlockState newState = checkState(world, pos, state);
		world.setBlockState(pos, newState, 3 | 1 << 6);
		IBlockState head = world.getBlockState(pos.up());
		if (head.getBlock() == newState.getBlock())
			world.setBlockState(pos.up(), head.withProperty(BlockWardrobe.RELY, newState.getValue(BlockWardrobe.RELY)), 3 | 1 << 6);
	}
	
	public void unLinkFacing() {
		setLinkFacing(null);
		setRelyType(null);
	}
	
	@Override
	public void update() {
		IBlockState state = worldObj.getBlockState(pos);
		if (state.getValue(BlockWardrobe.PART) == EnumPartType.HEAD)
			return;
		if (shouldLinkFacing()) {
			BlockPos linkPos = updateFacing(worldObj, pos, state);
			if (Always.isClient() && linkPos != null)
				checkState(worldObj, state.getBlock(), pos, state, linkPos, worldObj.getBlockState(linkPos));
		} else {
			TileEntity tile = worldObj.getTileEntity(pos.offset(linkFacing));
			if (!(tile instanceof TileWardrobe) || ((TileWardrobe) tile).shouldLinkFacing()) {
				System.out.println("unlink");
				unLinkFacing();
				if (Always.isClient()) {
					worldObj.setBlockState(pos, state.withProperty(BlockWardrobe.RELY, BlockWardrobe.EnumRelyType.NULL), 3 | 1 << 6);
					IBlockState head = worldObj.getBlockState(pos.up());
					if (head.getBlock() == state.getBlock())
						worldObj.setBlockState(pos.up(), head.withProperty(BlockWardrobe.RELY, BlockWardrobe.EnumRelyType.NULL), 3 | 1 << 6);
				}
			}
		}
		
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = super.serializeNBT();
		if (linkFacing != null && relyType != null) {
			nbt.setByte(NBT_KEY_LINK_FACING, (byte) linkFacing.ordinal());
			nbt.setByte(NBT_KEY_RELY_TYPE, (byte) relyType.ordinal());
		}
		return nbt;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		super.deserializeNBT(nbt);
		if (nbt.hasKey(NBT_KEY_LINK_FACING) && nbt.hasKey(NBT_KEY_RELY_TYPE)) {
			linkFacing = EnumFacing.values()[nbt.getByte(NBT_KEY_LINK_FACING)];
			relyType = BlockWardrobe.EnumRelyType.values()[nbt.getByte(NBT_KEY_RELY_TYPE)];
		}
	}

}
