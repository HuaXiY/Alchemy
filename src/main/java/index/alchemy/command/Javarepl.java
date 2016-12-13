package index.alchemy.command;

import mapi.xcore.Interpreter;
import net.minecraft.command.ICommandSender;

public class Javarepl {
	
	public static final void evaluate(ICommandSender sender, String expr) {
		Interpreter.todo(sender, expr);
	}
	
}
