package index.alchemy.api;

import index.alchemy.core.AlchemyInitHook;

public interface IRegister {
	
	public default void register() {
		AlchemyInitHook.init(this);
	}
	
	public default boolean shouldRegisterToGame() { return true; }

}
