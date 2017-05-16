package index.alchemy.dlcs.skin.core;

import java.util.Random;

import index.alchemy.block.AlchemyBlock;
import index.alchemy.interacting.WoodType;
import index.alchemy.util.Always;
import index.alchemy.util.BlockContainerAccess;
import index.project.version.annotation.Alpha;
import index.project.version.annotation.Beta;
import index.project.version.annotation.Omega;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
public class BlockWardrobe extends AlchemyBlock {
	
	@Omega
	public static enum EnumPartType implements IStringSerializable {
		HEAD,
		FOOT;

		public String toString() {
			return name().toLowerCase();
		}

		public String getName() {
			return name().toLowerCase();
		}
		
	}
	
	@Omega
	public static enum EnumRelyType implements IStringSerializable {
		LEFT,
		RIGHT,
		@Deprecated NULL;

		public String toString() {
			return name().toLowerCase();
		}

		public String getName() {
			return name().toLowerCase();
		}
		
		public EnumRelyType getOpposite() {
			if (this == LEFT)
				return RIGHT;
			if (this == RIGHT)
				return LEFT;
			return NULL;
		}
		
	}
	
	public static final PropertyEnum<EnumPartType> PART = PropertyEnum.<EnumPartType>create("part", EnumPartType.class);
	public static final PropertyEnum<EnumRelyType> RELY = PropertyEnum.<EnumRelyType>create("rely", EnumRelyType.class);
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public static final AxisAlignedBB AABB = new AxisAlignedBB(1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D);
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { PART, RELY, FACING });
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			BlockPos next = state.getValue(BlockWardrobe.PART) == EnumPartType.HEAD ? pos.down() : pos.up();
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 1 << 6 | 3);
			world.setBlockState(next, Blocks.AIR.getDefaultState(), 1 << 6 | 3);
		}
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) &&
				(super.canPlaceBlockAt(world, pos.up()) || canPlaceBlockAtStateDown(world.getBlockState(pos.up())));
	}
	
	public boolean canPlaceBlockAtStateDown(IBlockState state) {
		return state.getBlock() == this && state.getValue(PART) == EnumPartType.HEAD;
	}
	
	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return updateFacing(world, pos, getDefaultState()
				.withProperty(PART, EnumPartType.FOOT)
				.withProperty(RELY, EnumRelyType.LEFT)
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
	}
	
	public static IBlockState updateFacing(World world, BlockPos pos, IBlockState state) {
		EnumFacing facing = state.getValue(BlockWardrobe.FACING), temp = facing.rotateY();
		IBlockState next = world.getBlockState(pos.offset(temp));
		if (next.getBlock() == state.getBlock() && next.getValue(PART) == EnumPartType.FOOT
				&& next.getValue(FACING) == facing && checkFacing(world, pos.offset(temp), next).getValue(RELY) == EnumRelyType.LEFT)
			return state.withProperty(RELY, EnumRelyType.RIGHT);
		temp = temp.getOpposite();
		next = world.getBlockState(pos.offset(temp));
		if (next.getBlock() == state.getBlock() && next.getValue(FACING) == facing) {
			IBlockState next2 = world.getBlockState(pos.offset(temp, 2));
			if (next2.getBlock() != next.getBlock() || next2.getValue(PART) != next.getValue(PART)
					|| next2.getValue(FACING) != next.getValue(FACING) || next2.getValue(RELY) != EnumRelyType.RIGHT)
				world.setBlockState(pos.offset(temp), next.withProperty(RELY, EnumRelyType.RIGHT), 3 | 1 << 6);
		}
		return state;
	}
	
	public static IBlockState checkFacing(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(RELY) == EnumRelyType.RIGHT) {
			EnumFacing temp = state.getValue(FACING).rotateY();
			IBlockState next = world.getBlockState(pos.offset(temp));
			if (next.getBlock() != state.getBlock() || next.getValue(PART) != EnumPartType.FOOT
					|| next.getValue(FACING) != state.getValue(FACING) || next.getValue(RELY) != EnumRelyType.LEFT) {
				IBlockState result = state.withProperty(RELY, EnumRelyType.LEFT);
				world.setBlockState(pos, result, 3 | 1 << 6);
				return result;
			}
		}
		return state;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(PART) == EnumPartType.HEAD) {
			IBlockState foot = world.getBlockState(pos.down());
			if (foot.getBlock() == state.getBlock())
				return foot.getActualState(world, pos.down()).withProperty(PART, EnumPartType.HEAD);
		} else {
			EnumFacing facing = state.getValue(FACING);
			EnumRelyType relyType = state.getValue(RELY);
			BlockPos next = pos.offset(relyType == EnumRelyType.LEFT ? facing.rotateYCCW() : facing.rotateY());
			IBlockState nextState = world.getBlockState(next);
			if (nextState.getBlock() == state.getBlock() && nextState.getValue(RELY).getOpposite() == relyType)
				if (relyType == EnumRelyType.RIGHT || nextState.getValue(FACING) == facing)
					return state;
		}
		return state.withProperty(RELY, EnumRelyType.NULL);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getBlockState().getBaseState()
				.withProperty(PART, EnumPartType.values()[meta & 1])
				.withProperty(RELY, EnumRelyType.values()[meta >> 1 & 1])
				.withProperty(FACING, EnumFacing.values()[2 + (meta >> 2)]);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PART).ordinal() | state.getValue(RELY).ordinal() << 1 | state.getValue(FACING).ordinal() - 2 << 2;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (Always.isClient())
			openWardobeGui();
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public static void openWardobeGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
	}
	
	public BlockWardrobe(WoodType type) {
		super("wardrobe_" + type.toString(), Material.WOOD);
		this.type = type;
		setDefaultState(getDefaultState()
				.withProperty(FACING, EnumFacing.SOUTH)
				.withProperty(PART, EnumPartType.FOOT)
				.withProperty(RELY, EnumRelyType.LEFT));
		if (Always.isClient())
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(this), 0,
					new ModelResourceLocation(getRegistryName(), "facing=south,part=foot,rely=null"));
	}
	
	@Override
	public String getUnlocalizedName() {
		return "tile.wardrobe";
	}
	
	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.DECORATIONS;
	}
	
	public final WoodType type;
	
	// Proxy start
	
	@Override
	public int getLightValue(IBlockState state) {
		return Math.max(type.logState.getLightValue(), type.plankState.getLightValue());
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return Math.max(type.logState.getBlock().getLightValue(type.logState,
				new BlockContainerAccess(world, pos, type.logState), pos), 
				type.plankState.getBlock().getLightValue(type.plankState,
				new BlockContainerAccess(world, pos, type.plankState), pos));
	}
	
	@Override
	public Material getMaterial(IBlockState state) {
		return type.logState.getMaterial();
	}
	
	@Override
	public int getHarvestLevel(IBlockState state) {
		return type.logState.getBlock().getHarvestLevel(type.logState);
	}
	
	@Override
	public String getHarvestTool(IBlockState state) {
		return type.logState.getBlock().getHarvestTool(type.logState);
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
		return type.logState.getBlockHardness(world, pos);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		return type.logState.getPlayerRelativeBlockHardness(player, world, pos);
	}
	
	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return type.logState.getStrongPower(blockAccess, pos, side);
	}
	
	@Override
	public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
		return type.logState.getBlock().getWeakChanges(world, pos);
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return type.logState.getWeakPower(blockAccess, pos, side);
	}
	
	@Override
	public SoundType getSoundType() {
		return type.logState.getBlock().getSoundType();
	}
	
	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
		return type.logState.getBlock().getSoundType(type.logState, world, pos, entity);
	}
	
	@Override
	public float getExplosionResistance(Entity exploder) {
		return type.logState.getBlock().getExplosionResistance(exploder);
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		try {
			SkinCapability.updateCache(world, pos, type.logState);
			return type.logState.getBlock().getExplosionResistance(world, pos, exploder, explosion);
		} finally { SkinCapability.clearCache(); }
	}
	
	@Alpha
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return type.logState.getBlock().getFlammability(new BlockContainerAccess(world, pos, type.logState), pos, face);
	}
	
	@Alpha
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return type.logState.getBlock().getFireSpreadSpeed(new BlockContainerAccess(world, pos, type.logState), pos, face);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
		try {
			SkinCapability.updateCache(world, pos, type.logState);
			type.logState.getBlock().randomDisplayTick(type.logState, world, pos, rand);
		} finally { SkinCapability.clearCache(); }
	}

}
