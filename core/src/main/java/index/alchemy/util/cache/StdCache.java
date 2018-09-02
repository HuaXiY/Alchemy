package index.alchemy.util.cache;

import java.util.Map;

import com.google.common.collect.Maps;

public class StdCache<K, V> extends Cache<K, V> {
    
    private final Map<K, V> mapping;
    
    public StdCache() {
        this(false);
    }
    
    public StdCache(boolean linked) {
        mapping = linked ? Maps.newLinkedHashMap() : Maps.newHashMap();
    }
    
    public StdCache(Map<K, V> mapping) {
        this.mapping = mapping;
    }
    
    @Override
    public Map<K, V> getCacheMap() { return mapping; }
    
}
