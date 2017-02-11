package index.alchemy.dlcs.exnails.core;

import java.util.function.Function;

import index.alchemy.api.IBlockTemperature;
import index.alchemy.api.annotation.Patch;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

@Patch("net.minecraft.block.Block")
public class ExBlock extends Block implements IBlockTemperature {
	
	protected IBlockTemperature.TemperatureMap temperatureMap;
	
	private ExBlock(Material material) {
		super(material);
		temperatureMap = new IBlockTemperature.TemperatureMap();
	}
	
	private ExBlock(Material material, MapColor mapColor) {
		super(material, mapColor);
		temperatureMap = new IBlockTemperature.TemperatureMap();
	}

	@Override
	public void setBlockTemperature(IBlockState state, float temperature) {
		temperatureMap.mapping.put(state, temperature);
	}

	@Override
	public void setBlockTemperature(Function<IBlockState, Float> handle) {
		if (handle != null)
			temperatureMap.handle = handle;
	}
	
	@Override
	public float getBlockTemperature(EntityPlayer player, IBlockState state) {
		return temperatureMap.apply(state);
	}

}
