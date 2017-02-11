package index.alchemy.util.cache;

import java.util.Map;

import com.google.common.collect.Maps;

import index.alchemy.api.ICache;

public class ThreadContextCache<V> extends Cache<Thread, V> implements ICache.ContextCache<Thread, V> {
	
	protected final Map<Thread, V> mapping = Maps.newHashMap();
	
	@Override
	public Map<Thread, V> getCacheMap() { return mapping; }
	
	@Override
	public Thread getContext() { return Thread.currentThread(); }

}
