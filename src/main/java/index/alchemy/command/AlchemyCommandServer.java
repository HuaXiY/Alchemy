package index.alchemy.command;

import index.alchemy.core.AlchemyModLoader;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Omega
public abstract class AlchemyCommandServer extends AlchemyCommand {
	
	public AlchemyCommandServer() {
		AlchemyModLoader.addFMLEventCallback(FMLServerStartingEvent.class, e -> e.registerServerCommand(this));
	}

}
