package index.alchemy.command;

import index.alchemy.core.AlchemyEventSystem;
import index.project.version.annotation.Alpha;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.relauncher.Side;

import com.google.common.base.Joiner;

@Alpha
public class CommandSRun extends AlchemyCommandServer {
    
    @Override
    public String getName() {
        return "srun";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/srun <java-code>";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0)
            AlchemyEventSystem.addDelayedRunnable(Side.SERVER, p -> Javarepl.evaluate(sender, Joiner.on(' ').join(args)), 0);
    }
    
}
