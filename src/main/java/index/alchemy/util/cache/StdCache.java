package index.alchemy.util.cache;

import java.util.HashMap;
import java.util.Map;

public class StdCache<K, V> extends Cache<K, V> {
	
	private final Map<K, V> mapping = new HashMap<K, V>();

	@Override
	public Map<K, V> getCacheMap() { return mapping; }

}
