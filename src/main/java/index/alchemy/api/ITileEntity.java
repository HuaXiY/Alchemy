package index.alchemy.api;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;

public interface ITileEntity extends ITileEntityProvider {
	
	public Class<? extends TileEntity> getTileEntityClass();
	
	public String getTileEntityName();

}
