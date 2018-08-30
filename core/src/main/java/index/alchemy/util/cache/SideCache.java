package index.alchemy.util.cache;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import index.alchemy.util.SideHelper;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.relauncher.Side;

@Omega
public class SideCache<K, V> extends Cache<K, V> {
	
	protected final Map<Side, Map<K, V>> mapping = Maps.newEnumMap(Side.class);
	
	@Override
	public Map<K, V> getCacheMap() { return mapping.get(SideHelper.side()); }
	
	public SideCache() {
		for (Side side : Side.values())
			mapping.put(side, new HashMap<K, V>());
	}
	
}
