package index.alchemy.util.cache;

import java.util.Map;

import index.alchemy.util.SideHelper;

import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.Maps;

public class SideContextCache<V> extends Cache<Side, V> implements ICache.ContextCache<Side, V> {
    
    protected final Map<Side, V> mapping = Maps.newEnumMap(Side.class);
    
    @Override
    public Map<Side, V> getCacheMap() { return mapping; }
    
    @Override
    public Side getContext() { return SideHelper.side(); }
    
}
