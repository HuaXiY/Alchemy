package index.alchemy.command;

import index.alchemy.core.AlchemyModLoader;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public abstract class AlchemyCommandServer extends AlchemyCommand {
	
	public AlchemyCommandServer() {
		AlchemyModLoader.addFMLEventCallback(FMLServerStartingEvent.class, e -> e.registerServerCommand(this));
	}

}
