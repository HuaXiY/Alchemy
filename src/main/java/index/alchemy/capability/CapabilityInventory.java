package index.alchemy.capability;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.IInventoryProvider;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.inventory.AlchemyInventory;
import index.project.version.annotation.Omega;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Omega
public class CapabilityInventory extends AlchemyCapability<AlchemyInventory> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("inventory");
	
	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<? extends IInventoryProvider<?>> event) {
		event.addCapability(RESOURCE, event.getObject().initInventory());
	}

}
