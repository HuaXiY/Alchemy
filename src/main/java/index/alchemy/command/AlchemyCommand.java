package index.alchemy.command;

import index.alchemy.api.IRegister;
import net.minecraft.command.CommandBase;

public abstract class AlchemyCommand extends CommandBase implements IRegister {
	
	public AlchemyCommand() {
		register();
	}

}
