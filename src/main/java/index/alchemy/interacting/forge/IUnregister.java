package index.alchemy.interacting.forge;

import java.util.Collection;
import java.util.function.BiPredicate;

public interface IUnregister<K, V> {
	
	V unregistryKey(K key);
	
	void unregistryValue(V value);
	
	Collection<V> unregistryIf(BiPredicate<K, V> predicate);
	
	void onUnregistry();

}
