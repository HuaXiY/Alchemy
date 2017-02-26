package index.alchemy.block;

import index.alchemy.api.ITileEntity;
import index.alchemy.tile.TileEntitySpellWovenTable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSpellWovenTable extends AlchemyBlock implements ITileEntity {

	@Override
	public boolean isFullBlock(IBlockState state) { return false; }
	
	@Override
	public boolean isOpaqueCube(IBlockState state) { return false; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() { return BlockRenderLayer.CUTOUT; }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) { return new TileEntitySpellWovenTable();}

	@Override
	public Class<? extends TileEntity> getTileEntityClass() { return TileEntitySpellWovenTable.class; }

	@Override
	public String getTileEntityName() { return "spell_woven_table"; }
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntitySpellWovenTable) {
			TileEntitySpellWovenTable te = (TileEntitySpellWovenTable) world.getTileEntity(pos);
			if (te.transform != TRSRTransformation.identity())
				return ((IExtendedBlockState) state).withProperty(Properties.AnimationProperty, te.transform);
		}
		return state;
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { Properties.AnimationProperty });
	}
	
	public BlockSpellWovenTable() { super("spell_woven_table", Material.WOOD); }

}
