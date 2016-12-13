package index.alchemy.block;

import index.alchemy.api.IColorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyBlockColor extends AlchemyBlock implements IColorBlock {
	
	protected int color;
	
	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess access, BlockPos pos, int index) {
				return index == 1 ? color : -1;
			}
			
		};
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	public AlchemyBlockColor(String name, Material material, int color) {
		this(name, material, null, color);
	}

	public AlchemyBlockColor(String name, Material material, String icon, int color) {
		super(name, material, icon);
		this.color = color;
	}

}
