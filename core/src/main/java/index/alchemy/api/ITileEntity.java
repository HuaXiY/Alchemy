package index.alchemy.api;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public interface ITileEntity extends ITileEntityProvider {
    
    Class<? extends TileEntity> getTileEntityClass();
    
    ResourceLocation getTileEntityName();
    
}
