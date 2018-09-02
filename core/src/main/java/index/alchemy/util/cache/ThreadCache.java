package index.alchemy.util.cache;

import java.util.Map;

import index.project.version.annotation.Omega;

import com.google.common.collect.Maps;

@Omega
public class ThreadCache<K, V> extends Cache<K, V> {
    
    protected final Map<Thread, Map<K, V>> mapping = Maps.newHashMap();
    
    @Override
    public Map<K, V> getCacheMap() { return mapping.get(Thread.currentThread()); }
    
}
