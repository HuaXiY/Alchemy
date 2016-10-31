package index.alchemy.command;

import index.alchemy.util.Always;
import net.minecraftforge.client.ClientCommandHandler;

public abstract class AlchemyCommandClient extends AlchemyCommand {

	public AlchemyCommandClient() {
		if (Always.isClient())
			ClientCommandHandler.instance.registerCommand(this);
	}

}