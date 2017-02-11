package index.alchemy.command;

import index.alchemy.api.IRegister;
import index.project.version.annotation.Omega;
import net.minecraft.command.CommandBase;

@Omega
public abstract class AlchemyCommand extends CommandBase implements IRegister {
	
	public AlchemyCommand() { register(); }

}
