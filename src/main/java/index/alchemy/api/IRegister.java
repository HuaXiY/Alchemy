package index.alchemy.api;

import index.alchemy.core.AlchemyInitHook;

public interface IRegister {
	
	default void register() {
		AlchemyInitHook.init(this);
	}
	
	default boolean shouldRegisterToGame() { return true; }

}
