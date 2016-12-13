package index.alchemy.command;

import com.google.common.base.Joiner;

import index.alchemy.core.AlchemyEventSystem;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;

public class CommandCRun extends AlchemyCommandClient {

	@Override
	public String getCommandName() {
		return "crun";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/crun <java-code>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0)
			AlchemyEventSystem.addDelayedRunnable(Side.CLIENT, p -> Javarepl.evaluate(sender, Joiner.on(' ').join(args)), 0);
	}

}
