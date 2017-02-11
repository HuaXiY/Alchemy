package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.LoaderState.ModState;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.temperature.modifier.TimeModifier;

import static java.lang.Math.*;

import static index.alchemy.util.Tool.$;

@Hook.Provider
@Init(state = ModState.POSTINITIALIZED)
public class ExTimeModifier {
	
	public static final TemperatureDebugger.Modifier TEMPERATURE_DIFFERENCE_RATE = null, WEATHER_TARGET = null;
	
	public static void init() {
		Tool.load(TemperatureDebugger.Modifier.class);
		try {
			$(ExTimeModifier.class, "TEMPERATURE_DIFFERENCE_RATE<<",
					EnumHelper.addEnum(TemperatureDebugger.Modifier.class, "TEMPERATURE_DIFFERENCE_RATE",
					new Class<?>[]{ String.class, TemperatureDebugger.ModifierType.class }, "Temperature difference", TemperatureDebugger.ModifierType.RATE));
			$(ExTimeModifier.class, "WEATHER_TARGET<<",
					EnumHelper.addEnum(TemperatureDebugger.Modifier.class, "WEATHER_TARGET",
					new Class<?>[]{ String.class, TemperatureDebugger.ModifierType.class }, "Weather", TemperatureDebugger.ModifierType.TARGET));
		} catch (Exception e) {
			AlchemyRuntimeException.onException(
					new RuntimeException("Add Enum Exception: toughasnails.temperature.TemperatureDebugger$Modifier", e));
		}
	}
	
	@Hook("toughasnails.temperature.modifier.TimeModifier#modifyChangeRate")
	public static Hook.Result modifyChangeRate(TimeModifier modifier, World world, EntityPlayer player,
			int changeRate, TemperatureTrend trend) {
		ExNails.debugger.get(modifier).start(TEMPERATURE_DIFFERENCE_RATE, changeRate);
		int diffRate = abs(ExNails.debugger.get(modifier).targetTemperature -
				TemperatureHelper.getTemperatureData(player).getTemperature().getRawValue()) - 8;
		changeRate = max(changeRate - diffRate * diffRate, 20);
		ExNails.debugger.get(modifier).end(changeRate);
		return new Hook.Result().operationStack(2, changeRate);
	}
	
	@Hook("toughasnails.temperature.modifier.TimeModifier#modifyTarget")
	public static Hook.Result modifyTarget(TimeModifier modifier, World world, EntityPlayer player, Temperature temperature) {
		ExNails.debugger.get(modifier).start(WEATHER_TARGET, temperature.getRawValue());
		int temp = temperature.getRawValue();
		if (world.provider.isSurfaceWorld() && world.isDaytime() && !world.isRaining() && world.canSeeSky(player.getPosition()) &&
				world.getPrecipitationHeight(player.getPosition()).getY() < Math.ceil(player.posY + player.height))
			temp += round(2 * (-abs((float) (world.getWorldTime() + 6000) % 24000 - 12000) + 6000) / 6000F);
		Biome biome = world.getBiome(player.getPosition());
		if (world.isRaining() && biome.canRain())
			temp -= 2;
		ExNails.debugger.get(modifier).end((temperature = new Temperature(temp)).getRawValue());
		return new Hook.Result().operationStack(2, temperature);
	}

}
