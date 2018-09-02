package index.alchemy.util.cache;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import index.alchemy.api.ICastable;

public interface ICache<K, V> extends ICastable<ICache<K, V>> {
    
    interface ContextCache<K, V> extends ICache<K, V> {
        
        K getContext();
        
        default V get() { return get(getContext()); }
        
        default V add(V val) { return add(getContext(), val); }
        
        default V del() { return del(getContext()); }
        
        default boolean has() { return has(getContext()); }
        
    }
    
    class EmptyCache<K, V> implements ICache<K, V> {
        
        public int getMaxCache() { return -1; }
        
        public <T extends ICache<K, V>> T setMaxCache(int max) { return cast(); }
        
        @Override
        public <T extends ICache<K, V>> T setOnMissGet(Supplier<V> onMiss) { return cast(); }
        
        @Override
        public <T extends ICache<K, V>> T setOnMissMap(Function<K, V> onMiss) { return cast(); }
        
        @Override
        public Function<K, V> getOnMiss() { return null; }
        
        @Override
        public Map<K, V> getCacheMap() { return null; }
        
        @Override
        public V get(K key) { return null; }
        
        @Override
        public V add(K key, V val) { return val; }
        
        @Override
        public V del(K key) { return null; }
        
        public int size() { return 0; }
        
        public boolean has(K k) { return false; }
        
    }
    
    int getMaxCache();
    
    <T extends ICache<K, V>> T setMaxCache(int max);
    
    Map<K, V> getCacheMap();
    
    <T extends ICache<K, V>> T setOnMissGet(Supplier<V> onMiss);
    
    <T extends ICache<K, V>> T setOnMissMap(Function<K, V> onMiss);
    
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
    
    default int size() {
        return getCacheMap().size();
    }
    
    default boolean has(K k) {
        return getCacheMap().containsKey(k);
    }
    
}
