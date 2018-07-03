package index.alchemy.capability;

import java.lang.reflect.Modifier;

import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.inventory.AlchemyInventory;
import index.alchemy.util.$;
import index.project.version.annotation.Omega;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static index.alchemy.util.$.$;

@Omega
@Loading
public class AlchemyCapabilityLoader {
	
	@CapabilityInject(CapabilityInventory.class)
	public static final Capability<AlchemyInventory> inventory = null;
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		if ($.isInstance(AlchemyCapability.class, clazz) && !Modifier.isAbstract(clazz.getModifiers()))
			AlchemyModLoader.addFMLEventCallback(FMLPreInitializationEvent.class, () -> $(clazz, "new"));
	}
	
}