package index.alchemy.block.proxy;

import index.alchemy.annotation.Change;
import index.alchemy.api.ITileEntity;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.tile.TileEntityCauldron;
import net.minecraft.block.BlockCauldron;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Change
public class PBlockCauldron extends BlockCauldron implements ITileEntity {

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
	
	public PBlockCauldron() {
		AlchemyInitHook.init_impl(this);
	}

}
