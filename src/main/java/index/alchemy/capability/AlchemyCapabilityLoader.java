package index.alchemy.capability;

import index.alchemy.api.annotation.Init;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.inventory.InventoryBauble;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyCapabilityLoader {
	
	public static final String TYPE = "Capability";
	
	@CapabilityInject(CapabilityTimeLeap.class)
	public static final Capability<TimeSnapshot> time_leap = null;
	
	@CapabilityInject(CapabilityBauble.class)
	public static final Capability<InventoryBauble> bauble = null;
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (Class<?> clazz : AlchemyModLoader.instance_map.get(TYPE))
			try {
				clazz.newInstance();
			} catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
	}

}