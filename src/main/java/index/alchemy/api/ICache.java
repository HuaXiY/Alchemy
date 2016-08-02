package index.alchemy.api;

import java.util.Map;

public interface ICache<K, V> {
	
	public int getMaxCache();
	
	public ICache<K, V> setMaxCache(int max);
	
	public Map<K, V> getCacheMap();
	
	public V get(K key);
	
	public V add(K key, V val);
	
	public V del(K key);

}
