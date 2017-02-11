package index.alchemy.api;

import java.util.Map;
import java.util.Objects;

public interface ICache<K, V> {
	
	interface ContextCache<K, V> extends ICache<K, V> {
		
		K getContext();
		
		default V get() { return get(getContext()); }
		
		default V add(V val) { return add(getContext(), val); }
		
		default V del() { return del(getContext()); }
		
	}
	
	int getMaxCache();
	
	ICache<K, V> setMaxCache(int max);
	
	Map<K, V> getCacheMap();
	
	default V get(K key) {
		return getCacheMap().get(key);
	}
	
	default V add(K key, V val) {
		Map<K, V> cache = getCacheMap();
		if (getMaxCache() > 0 && cache.size() > getMaxCache()) cache.clear();
		return cache.put(key, val);
	}
	
	default V del(K key) {
		return getCacheMap().remove(key);
	}
	
	default boolean equals(K key, V val) {
		return Objects.equals(get(key), val);
	}

}
