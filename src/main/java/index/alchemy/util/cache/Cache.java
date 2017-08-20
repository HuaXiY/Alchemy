package index.alchemy.util.cache;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Cache<K, V> implements ICache<K, V> {
	
	private int max;
	protected Function<K, V> onMiss;
	
	public Cache() {
		this(-1);
	}
	
	public Cache(int max) {
		this(max, null);
	}
	
	public Cache(Supplier<V> onMiss) {
		this(-1, null);
	}
	
	public Cache(int max, Function<K, V> onMiss) {
		this.onMiss = onMiss;
	}
	
	@Override
	public V onMiss(K key) {
		return onMiss == null ? ICache.super.onMiss(key) : onMiss.apply(key);
	}
	
	@Override
	public ICache<K, V> setOnMiss(Supplier<V> onMiss) {
		this.onMiss = key -> onMiss.get();
		return this;
	}
	
	@Override
	public ICache<K, V> setOnMiss(Function<K, V> onMiss) {
		this.onMiss = onMiss;
		return this;
	}
	
	@Override
	public Function<K, V> getOnMiss() {
		return onMiss;
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
