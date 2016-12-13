package index.alchemy.util.cache;

import java.util.HashMap;
import java.util.Map;

import index.alchemy.util.Always;
import net.minecraftforge.fml.relauncher.Side;

public class SideCache<K, V> extends Cache<K, V> {
	
	private final Map<Side, Map<K, V>> mapping = new HashMap<Side, Map<K,V>>();
	
	public SideCache() {
		for (Side side : Side.values())
			mapping.put(side, new HashMap<K, V>());
	}

	@Override
	public Map<K, V> getCacheMap() {
		return mapping.get(Always.getSide());
	}

}