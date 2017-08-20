package index.alchemy.util;

import javax.annotation.Nullable;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.interacting.forge.IUnregister;
import index.project.version.annotation.Omega;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Omega
public interface ForgeHelper {
	
	@Nullable
	static <T extends IForgeRegistryEntry<T>> ForgeRegistry<T> getForgeRegistry(Class<T> type) {
		IForgeRegistry<T> result = GameRegistry.findRegistry(type);
		if (result instanceof ForgeRegistry)
			return (ForgeRegistry<T>) result;
		else
			AlchemyRuntimeException.onException(new RuntimeException(
					new ClassCastException(result.getClass().getName() + " cannot be cast to " + ForgeRegistry.class.getName())));
		return null;
	}
	
	static <T extends IForgeRegistryEntry<T>> IUnregister<ResourceLocation, T> getUnregister(Class<T> type) {
		IForgeRegistry<T> result = GameRegistry.findRegistry(type);
		if (result instanceof IUnregister)
			return (IUnregister<ResourceLocation, T>) result;
		else
			AlchemyRuntimeException.onException(new RuntimeException(
					new ClassCastException(result.getClass().getName() + " cannot be cast to " + IForgeRegistry.class.getName())));
		return null;
	}

}
