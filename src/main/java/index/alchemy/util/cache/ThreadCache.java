package index.alchemy.util.cache;

import java.util.Map;

import com.google.common.collect.Maps;

import index.project.version.annotation.Omega;

@Omega
public class ThreadCache<K, V> extends Cache<K, V> {
	
	protected final Map<Thread, Map<K, V>> mapping = Maps.newHashMap();
	
	@Override
	public Map<K, V> getCacheMap() { return mapping.get(Thread.currentThread()); }
	
}
