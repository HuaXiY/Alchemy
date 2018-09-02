package index.alchemy.api;

import java.util.concurrent.Callable;

import index.alchemy.util.TypeResolver;

import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface ICapability<T> extends IStorage<T>, Callable<T> {
    
    @SuppressWarnings("unchecked")
    default Class<T> getDataClass() {
        return (Class<T>) TypeResolver.resolveRawArgument(getClass(), ICapability.class);
    }
    
}