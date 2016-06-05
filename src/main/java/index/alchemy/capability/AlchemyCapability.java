package index.alchemy.capability;

import index.alchemy.api.ICapability;
import index.alchemy.api.IRegister;
import index.alchemy.core.AlchemyInitHook;

public abstract class AlchemyCapability<T> implements ICapability<T>, IRegister {
	
	@Override
	public T call() throws Exception {
		return getDataClass().newInstance();
	}

	public AlchemyCapability() {
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}