package index.alchemy.util.cache;

import java.util.Map;
import java.util.WeakHashMap;

public class WeakRefCache<K, V> extends Cache<K, V> {
    
    private Map<K, V> mapping = new WeakHashMap<K, V>();
    
    @Override
    public Map<K, V> getCacheMap() {
        return mapping;
    }
    
}
