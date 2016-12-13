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
	
}