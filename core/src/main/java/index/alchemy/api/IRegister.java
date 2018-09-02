package index.alchemy.api;

import java.util.function.Consumer;

import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.debug.AlchemyRuntimeException;

import net.minecraft.creativetab.CreativeTabs;

public interface IRegister {
    
    Consumer<IRegister> impl = AlchemyInitHook::init;
    
    default void register() {
        if (impl != null)
            impl.accept(this);
        else
            AlchemyRuntimeException.onException(new NullPointerException("impl"));
    }
    
    default boolean shouldRegisterToGame() { return true; }
    
    default CreativeTabs getCreativeTab() { return null; }
    
}
