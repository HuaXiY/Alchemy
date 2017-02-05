package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.DynamicNumber;
import index.alchemy.util.Tool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.LoaderState.ModState;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.modifier.TimeModifier;

import static java.lang.Math.*;
import static index.alchemy.util.Tool.$;

@Hook.Provider
@Init(state = ModState.POSTINITIALIZED)
public class ExTimeModifier {
	
	public static final TemperatureDebugger.Modifier SUNLIGHT_TARGET = null;
	
	public static void init() {
		Tool.load(TemperatureDebugger.Modifier.class);
		try {
			$(ExTimeModifier.class, "SUNLIGHT_TARGET<<",
					EnumHelper.addEnum(TemperatureDebugger.Modifier.class, "SUNLIGHT_TARGET",
					new Class<?>[]{ String.class, TemperatureDebugger.ModifierType.class }, "Sunlight", TemperatureDebugger.ModifierType.TARGET));
		} catch (Exception e) {
			AlchemyRuntimeException.onException(
					new RuntimeException("Add Enum Exception: toughasnails.temperature.TemperatureDebugger$Modifier", e));
		}
	}
	
	@Hook("toughasnails.temperature.modifier.TimeModifier#modifyTarget")
	public static Hook.Result modifyTarget(TimeModifier modifier, World world, EntityPlayer player, Temperature temperature) {
		ExNails.debugger.get(modifier).start(SUNLIGHT_TARGET, temperature.getRawValue());
		DynamicNumber<Integer> number = new DynamicNumber(temperature.getRawValue());
		if (world.provider.isSurfaceWorld() && world.isDaytime() && !world.isRaining() && world.canSeeSky(player.getPosition()) &&
				world.getPrecipitationHeight(player.getPosition()).getY() < Math.ceil(player.posY + player.height))
			number.value += round(2 * (-abs((float) (world.getWorldTime() + 6000) % 24000 - 12000) + 6000) / 6000F);
		ExNails.debugger.get(modifier).end((temperature = new Temperature(number.intValue())).getRawValue());
		return new Hook.Result().operationStack(2, temperature);
	}

}
