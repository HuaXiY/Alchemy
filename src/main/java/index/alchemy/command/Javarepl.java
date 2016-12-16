package index.alchemy.command;

import index.project.version.annotation.Alpha;
import mapi.xcore.Interpreter;
import net.minecraft.command.ICommandSender;

@Alpha
public class Javarepl {
	
	public static final void evaluate(ICommandSender sender, String expr) {
		Interpreter.todo(sender, expr);
	}
	
}
