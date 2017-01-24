package index.alchemy.api;

import java.util.function.Consumer;

import net.minecraft.creativetab.CreativeTabs;

public interface IRegister {
	
	Consumer<IRegister> impl = null;
	
	default void register() {
		if (impl != null)
			impl.accept(this);
	}
	
	default boolean shouldRegisterToGame() { return true; }
	
	default CreativeTabs getCreativeTab() { return null; }

}
