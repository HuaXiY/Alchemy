package index.alchemy.block.proxy;

import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.ITileEntity;
import net.minecraft.block.BlockCauldron;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PBlockCauldron extends BlockCauldron implements ITileEntity {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public Class<? extends TileEntity> getTileEntityClass() {
		return null;
	}

	@Override
	public String getTileEntityName() {
		return getUnlocalizedName();
	}
	
	public PBlockCauldron() {
		AlchemyInitHook.init_impl(this);
	}

}
