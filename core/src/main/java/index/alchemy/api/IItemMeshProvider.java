package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemMeshProvider {
    
    @Nullable
    @SideOnly(Side.CLIENT)
    ItemMeshDefinition getItemMesh();
    
    @Nullable
    @SideOnly(Side.CLIENT)
    ResourceLocation[] getItemVariants();
    
}
