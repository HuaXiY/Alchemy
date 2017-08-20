package index.alchemy.util.cache;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ICache<K, V> {
	
	interface ContextCache<K, V> extends ICache<K, V> {
		
		K getContext();
		
		default V get() { return get(getContext()); }
		
		default V add(V val) { return add(getContext(), val); }
		
		default V del() { return del(getContext()); }
		
	}
	
	interface EmptyCache<K, V> extends ICache<K, V> {
		
		default int getMaxCache() { return -1; }
		
		default EmptyCache<K, V> setMaxCache(int max) { return this; }
		
		@Override
		default EmptyCache<K, V> setOnMiss(Function<K, V> onMiss) { return this; }
		
		@Override
		default Function<K, V> getOnMiss() { return null; }
		
		@Override
		default Map<K, V> getCacheMap() { return null; }
		
		@Override
		default V get(K key) { return null; }
		
		@Override
		default V add(K key, V val) { return val; }
		
		@Override
		default V del(K key) { return null; }
		
	}
	
	int getMaxCache();
	
	ICache<K, V> setMaxCache(int max);
	
	Map<K, V> getCacheMap();
	
	ICache<K, V> setOnMiss(Supplier<V> onMiss);
	
	ICache<K, V> setOnMiss(Function<K, V> onMiss);
	
	Function<K, V> getOnMiss();
	
	default V onMiss(K key) { return null; }
	
	default V get(K key) {
		V result = getCacheMap().get(key);
		if (result == null && !getCacheMap().containsKey(key))
			add(key, result = onMiss(key));
		return result;
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
