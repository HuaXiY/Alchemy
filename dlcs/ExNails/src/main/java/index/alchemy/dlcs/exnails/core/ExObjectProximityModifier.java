package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.IBlockTemperature;
import index.alchemy.api.annotation.Patch;
import index.project.version.annotation.Omega;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.temperature.TemperatureDebugger.Modifier;
import toughasnails.temperature.modifier.ObjectProximityModifier;

@Omega
@Patch("toughasnails.temperature.modifier.ObjectProximityModifier")
public class ExObjectProximityModifier extends ObjectProximityModifier {
	
	@Patch.Exception
	private ExObjectProximityModifier(TemperatureDebugger debugger) {
		super(debugger);
	}
	
	@Override
	public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
		return changeRate;
	}

	@Override
	public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
		int temperatureLevel = temperature.getRawValue();
		int newTemperatureLevel = temperatureLevel;
		BlockPos playerPos = player.getPosition();
		
		float blockTemperatureModifier = 0.0F;
		int maxSq = 3 * 3 + 2 * 2 + 3 * 3 + 2;
		for (int x = -3; x <= 3; x++)
			for (int y = -2; y <= 2; y++)
				for (int z = -3; z <= 3; z++) {
					BlockPos pos = playerPos.add(x, y - 1, z);
					IBlockState state = world.getBlockState(pos);

					blockTemperatureModifier += getBlockTemperature(player, state) * (maxSq - pos.distanceSq(playerPos)) / maxSq;
				}
		
		debugger.start(Modifier.NEARBY_BLOCKS_TARGET, newTemperatureLevel);
		newTemperatureLevel += blockTemperatureModifier;
		debugger.end(newTemperatureLevel);
		
		return new Temperature(newTemperatureLevel);
	}

	public static float getBlockTemperature(EntityPlayer player, IBlockState state) {
		boolean flag = !state.getBlock().getClass().getName().startsWith("net.minecraft.");
		World world = player.worldObj;
		Material material = state.getMaterial();
		if (material == Material.FIRE)
			return 1.0F;
		else if (material == Material.LAVA)
			return 1.5F;
		if (state.getBlock() instanceof IBlockTemperature) {
			IBlockTemperature temperature = (IBlockTemperature) state.getBlock();
			return temperature.getBlockTemperature(player, state);
		}
		return 0.0F;
	}
	
}
