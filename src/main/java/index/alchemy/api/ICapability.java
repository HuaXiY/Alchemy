package index.alchemy.api;

import java.util.concurrent.Callable;

import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface ICapability<T> extends IStorage<T>, Callable<T> {
	
	 Class<T> getDataClass();

}