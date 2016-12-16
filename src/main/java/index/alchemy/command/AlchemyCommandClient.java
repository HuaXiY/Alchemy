package index.alchemy.command;

import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import net.minecraftforge.client.ClientCommandHandler;

@Omega
public abstract class AlchemyCommandClient extends AlchemyCommand {

	public AlchemyCommandClient() {
		if (Always.isClient())
			ClientCommandHandler.instance.registerCommand(this);
	}

}