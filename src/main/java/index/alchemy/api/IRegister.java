package index.alchemy.api;

import index.alchemy.core.AlchemyInitHook;
import net.minecraft.creativetab.CreativeTabs;

public interface IRegister {
	
	default void register() {
		AlchemyInitHook.init(this);
	}
	
	default boolean shouldRegisterToGame() { return true; }
	
	default CreativeTabs getCreativeTab() { return null; }

}
