package index.alchemy.command;

import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Config;
import index.project.version.annotation.Omega;

import net.minecraft.command.CommandBase;

@Omega
public abstract class AlchemyCommand extends CommandBase implements IRegister {
    
    public static final String CATEGORY_COMMAND = "command";
    
    @Config(category = CATEGORY_COMMAND, comment = "Do not check permission to use the command.")
    public static boolean do_not_check_permission = false;
    
    public AlchemyCommand() { register(); }
    
}
