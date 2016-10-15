package baubles.api.cap;

import index.alchemy.capability.CapabilityBauble;
import index.alchemy.inventory.InventoryBauble;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class BaublesCapabilities {
	
	@CapabilityInject(CapabilityBauble.class)
	public static Capability<InventoryBauble> CAPABILITY_BAUBLES = null;

}
