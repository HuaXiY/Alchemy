package index.alchemy.util.cache;

import java.util.Map;

import index.alchemy.api.ICache;

public abstract class Cache<K, V> implements ICache<K, V> {
	
	private int max;
	
	public Cache() {
		this(-1);
	}
	
	public Cache(int max) {
		this.max = max;
	}
	
	public int getMaxCache() {
		return max;
	}
	
	public Cache<K, V> setMaxCache(int max) {
		this.max = max;
		return this;
	}
	
	public abstract Map<K, V> getCacheMap();
	
	public V get(K key) {
		return getCacheMap().get(key);
	}
	
	public V add(K key, V val) {
		Map<K, V> cache = getCacheMap();
		if (max > 0 && cache.size() > max) cache.clear();
		return cache.put(key, val);
	}
	
	public V del(K key) {
		return getCacheMap().remove(key);
	}
	
}