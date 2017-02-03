package index.alchemy.dlcs.exnails.core;

import index.alchemy.command.AlchemyCommandServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandReloadThirst extends AlchemyCommandServer {

	@Override
	public String getCommandName() {
		return "reload-thirst";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/reload-thirst";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			ExThirstLoader.loadConfig(ExNails.ITEM_THIRST_CFG);
		} catch (Exception e) { throw new CommandException(e.toString()); }
	}

}
