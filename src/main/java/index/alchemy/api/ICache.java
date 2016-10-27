package index.alchemy.api;

import java.util.Map;

public interface ICache<K, V> {
	
	int getMaxCache();
	
	ICache<K, V> setMaxCache(int max);
	
	Map<K, V> getCacheMap();
	
	V get(K key);
	
	V add(K key, V val);
	
	V del(K key);

}
