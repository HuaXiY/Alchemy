package index.alchemy.command;

import java.lang.reflect.Modifier;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.$;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import static index.alchemy.util.$.$;

@Omega
@Loading
@Init(state = ModState.INITIALIZED)
public class AlchemyCommandLoader {
	
	public static void init(Class<?> clazz) {
		if ($.isInstance(AlchemyCommand.class, clazz) && !Modifier.isAbstract(clazz.getModifiers()))
			AlchemyModLoader.addFMLEventCallback(FMLInitializationEvent.class, () -> $(clazz, "new"));
	}

}
