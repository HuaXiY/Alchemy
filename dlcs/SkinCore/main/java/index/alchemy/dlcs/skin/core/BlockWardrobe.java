package index.alchemy.dlcs.skin.core;

import java.util.Random;

import index.alchemy.api.ITileEntity;
import index.alchemy.block.AlchemyBlock;
import index.alchemy.interacting.WoodType;
import index.project.version.annotation.Beta;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
public class BlockWardrobe extends AlchemyBlock implements ITileEntity {
	
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
	
	public static enum EnumRelyType implements IStringSerializable {
		NULL,
		LEFT,
		RIGHT;

		public String toString() {
			return name().toLowerCase();
		}

		public String getName() {
			return name().toLowerCase();
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
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
				.withProperty(RELY, EnumRelyType.NULL)
				.withProperty(PART, EnumPartType.FOOT);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return meta == 0 ? getDefaultState() : getBlockState().getBaseState()
				.withProperty(PART, EnumPartType.values()[meta & 1])
				.withProperty(FACING, EnumFacing.values()[Math.max(meta >> 1, 2)]);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PART).ordinal() | state.getValue(FACING).ordinal() << 1;
	}
	
	@Override
	public String getTileEntityName() {
		return "skin_wardrobe";
	}
	
	@Override
	public Class<? extends TileEntity> getTileEntityClass() {
		return TileWardrobe.class;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileWardrobe();
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
	@SideOnly(Side.CLIENT)
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
		return true;
	}
	
	public static String getRegistryName(WoodType type) {
		ResourceLocation location = type.log.getItem().getRegistryName();
		String name = "wardrobe_" + location.getResourceDomain() + "_T_" + location.getResourcePath() + "_T_" + type.log.getMetadata();
		while (Block.getBlockFromName("skin:" + name) != null)
			name += "_F";
		return name;
	}
	
	public BlockWardrobe(WoodType type) {
		super(getRegistryName(type), Material.WOOD);
		this.type = type;
		//setRegistryName(name);
		setDefaultState(getDefaultState()
				.withProperty(FACING, EnumFacing.SOUTH)
				.withProperty(PART, EnumPartType.FOOT)
				.withProperty(RELY, EnumRelyType.NULL));
		//ItemBlock item = new ItemBlock(this);
		//item.setCreativeTab(CreativeTabs.DECORATIONS);
		//item.setRegistryName(getRegistryName());
		//GameRegistry.register(this);
		//GameRegistry.register(item);
	}
	
	@Override
	public String getUnlocalizedName() {
		return "wardrobe";
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
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return type.logState.getBlockHardness(worldIn, pos);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
		return type.logState.getPlayerRelativeBlockHardness(player, worldIn, pos);
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
		try {
			SkinCapability.updateCache(world, pos, type.logState);
			return type.logState.getBlock().getSoundType(type.logState, world, pos, entity);
		} finally { SkinCapability.clearCache(); }
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
	
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		try {
			SkinCapability.updateCache(world, pos, type.logState);
			return type.logState.getBlock().getFlammability(world, pos, face);
		} finally { SkinCapability.clearCache(); }
	}
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		try {
			SkinCapability.updateCache(world, pos, type.logState);
			return type.logState.getBlock().getFireSpreadSpeed(world, pos, face);
		} finally { SkinCapability.clearCache(); }
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
