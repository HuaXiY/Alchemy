package index.alchemy.dlcs.exnails.core;

import index.alchemy.command.AlchemyCommandServer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandReloadPotion extends AlchemyCommandServer {

	@Override
	public String getCommandName() {
		return "reload-potion";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/reload-potion";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			ExPotionLoader.loadConfig(ExNails.ITEM_POTION_CFG);
		} catch (Exception e) { throw new CommandException(e.toString()); }
	}

}
