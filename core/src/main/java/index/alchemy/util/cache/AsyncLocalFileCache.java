package index.alchemy.util.cache;

import java.io.File;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

public class AsyncLocalFileCache extends Cache<String, File> {
    
    protected final File cachePath;
    protected final Map<String, File> folderMap;
    protected final BiFunction<String, AsyncLocalFileCache, File> asyncHandler;
    
    public AsyncLocalFileCache(File cachePath, Map<String, File> folderMap, BiFunction<String, AsyncLocalFileCache, File> asyncHandler) {
        this.cachePath = cachePath;
        this.folderMap = folderMap;
        this.asyncHandler = asyncHandler;
    }
    
    public File getCachePath() { return cachePath; }
    
    @Override
    public Map<String, File> getCacheMap() { return folderMap; }
    
    @Nullable
    @Override
    public File get(String key) {
        File result = super.get(key);
        if (result == null)
            return asyncHandler.apply(key, this);
        return result;
    }
    
}
