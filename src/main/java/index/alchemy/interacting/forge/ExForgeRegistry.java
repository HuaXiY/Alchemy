package index.alchemy.interacting.forge;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import index.alchemy.api.annotation.Patch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

@Patch("net.minecraftforge.registries.ForgeRegistry")
public class ExForgeRegistry<K extends ResourceLocation, V extends IForgeRegistryEntry<V>> extends ForgeRegistry<V>
		implements IUnregister<ResourceLocation, V> {
	
	protected boolean recoveryIsModifiable, recoveryIsFrozen;

	@Patch.Exception
	ExForgeRegistry(Class superType, ResourceLocation defaultKey, int min, int max, CreateCallback create,
			AddCallback add, ClearCallback clear, RegistryManager stage, boolean allowOverrides, boolean isModifiable, DummyFactory dummyFactory) {
		super(superType, defaultKey, min, max, create, add, clear, stage, allowOverrides, isModifiable, dummyFactory);
	}

	@Override
	public V unregistryKey(ResourceLocation key) {
		checkFlag();
		V result = remove(key);
		onUnregistry();
		return result;
	}

	@Override
	public void unregistryValue(V value) {
		checkFlag();
		remove(getKey(value));
		onUnregistry();
	}

	@Override
	public Collection<V> unregistryIf(BiPredicate<ResourceLocation, V> predicate) {
		checkFlag();
		Set<V> result = Sets.newHashSet();
		Maps.newHashMap(names).forEach((key, value) -> {
			if (predicate.test(key, value))
				result.add(remove(key));
		});
		result.remove(null);
		onUnregistry();
		return result;
	}
	
	protected void checkFlag() {
		if (!isModifiable) {
			isModifiable = true;
			recoveryIsModifiable = true;
		}
		if (isFrozen) {
			isFrozen = false;
			recoveryIsFrozen = true;
		}
	}

	@Override
	public void onUnregistry() {
		if (recoveryIsModifiable) {
			isModifiable = !isModifiable;
			recoveryIsModifiable = false;
		}
		if (recoveryIsFrozen) {
			isFrozen = !isFrozen;
			recoveryIsFrozen = false;
		}
	}

}
