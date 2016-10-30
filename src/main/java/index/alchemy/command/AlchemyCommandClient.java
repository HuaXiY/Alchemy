package index.alchemy.command;

import net.minecraftforge.client.ClientCommandHandler;

public abstract class AlchemyCommandClient extends AlchemyCommand {

	public AlchemyCommandClient() {
		ClientCommandHandler.instance.registerCommand(this);
	}

}