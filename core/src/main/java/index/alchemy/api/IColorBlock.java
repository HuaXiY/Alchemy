package index.alchemy.api;

import net.minecraft.client.renderer.color.IBlockColor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IColorBlock {
    
    @SideOnly(Side.CLIENT)
    IBlockColor getBlockColor();
    
}