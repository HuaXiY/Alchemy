package index.alchemy.capability;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.IInventoryProvider;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.inventory.AlchemyInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityInventory extends AlchemyCapability<AlchemyInventory> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("inventory");
	
	@Override
	public Class<AlchemyInventory> getDataClass() {
		return AlchemyInventory.class;
	}
	
	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<? extends IInventoryProvider> event) {
		event.addCapability(RESOURCE, event.getObject().initInventory());
	}

}
