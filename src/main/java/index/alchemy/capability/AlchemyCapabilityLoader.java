package index.alchemy.capability;

import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.inventory.AlchemyInventory;
import index.alchemy.inventory.InventoryBauble;
import index.project.version.annotation.Omega;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

@Omega
public class AlchemyCapabilityLoader {
	
	@CapabilityInject(CapabilityTimeLeap.class)
	public static final Capability<TimeSnapshot> time_leap = null;
	
	@CapabilityInject(CapabilityBauble.class)
	public static final Capability<InventoryBauble> bauble = null;
	
	@CapabilityInject(CapabilityInventory.class)
	public static final Capability<AlchemyInventory> inventory = null;
	
}