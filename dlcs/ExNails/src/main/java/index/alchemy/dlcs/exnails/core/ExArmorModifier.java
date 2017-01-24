package index.alchemy.dlcs.exnails.core;

import java.util.Objects;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.IItemTemperature;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Hook.Type;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.DynamicNumber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.temperature.modifier.ArmorModifier;
import toughasnails.temperature.modifier.TemperatureModifier;

import static index.alchemy.util.Tool.$;

@Hook.Provider
@Field.Provider
public class ExArmorModifier {
	
	public static final TemperatureDebugger.Modifier BAUBLE_TARGET = null, BAUBLE_RATE = null;
	
	public static final IFieldAccess<TemperatureModifier, TemperatureDebugger> debugger = null;
	
	@Hook(value = "toughasnails.temperature.TemperatureDebugger$Modifier#<clinit>", type = Type.TAIL, isStatic = true)
	public static void clinit() {
		try {
			$(ExArmorModifier.class, "BAUBLE_TARGET<",
					EnumHelper.addEnum(TemperatureDebugger.Modifier.class, "BAUBLE_TARGET",
					new Class<?>[]{ String.class, TemperatureDebugger.ModifierType.class }, "Bauble", TemperatureDebugger.ModifierType.TARGET));
			$(ExArmorModifier.class, "BAUBLE_RATE<",
					EnumHelper.addEnum(TemperatureDebugger.Modifier.class, "BAUBLE_RATE",
					new Class<?>[]{ String.class, TemperatureDebugger.ModifierType.class }, "Bauble", TemperatureDebugger.ModifierType.RATE));
		} catch (Exception e) {
			AlchemyRuntimeException.onException(
					new RuntimeException("Add Enum Exception: toughasnails.temperature.TemperatureDebugger$Modifier", e));
		}
	}

	@Hook("toughasnails.temperature.modifier.ArmorModifier#modifyChangeRate")
	public static Hook.Result modifyChangeRate(ArmorModifier modifier, World world, EntityPlayer player,
			int changeRate, TemperatureTrend trend) {
		debugger.get(modifier).start(BAUBLE_RATE, changeRate);
		DynamicNumber number = new DynamicNumber(changeRate);
		player.getCapability(AlchemyCapabilityLoader.bauble, null)
				.stream()
				.filter(Objects::nonNull)
				.filter(i -> i.getItem() instanceof IItemTemperature)
				.forEach(i -> number.value = ((IItemTemperature) i.getItem()).modifyChangeRate(world, player,
						number.floatValue(), trend == TemperatureTrend.STILL ? 0 : trend == TemperatureTrend.INCREASING ? 1 : -1));
		debugger.get(modifier).end(changeRate = number.intValue());
		return new Hook.Result().operationStack(2, changeRate);
	}
	
	@Hook("toughasnails.temperature.modifier.ArmorModifier#modifyTarget")
	public static Hook.Result modifyTarget(ArmorModifier modifier, World world, EntityPlayer player, Temperature temperature) {
		debugger.get(modifier).start(BAUBLE_TARGET, temperature.getRawValue());
		DynamicNumber number = new DynamicNumber(temperature.getRawValue());
		player.getCapability(AlchemyCapabilityLoader.bauble, null)
				.stream()
				.filter(Objects::nonNull)
				.filter(i -> i.getItem() instanceof IItemTemperature)
				.forEach(i -> number.value = ((IItemTemperature) i.getItem()).modifyTarget(world, player, number.floatValue()));
		debugger.get(modifier).end((temperature = new Temperature(number.intValue())).getRawValue());
		return new Hook.Result().operationStack(2, temperature);
	}

}
