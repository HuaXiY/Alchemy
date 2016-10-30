package index.alchemy.command;

import index.alchemy.api.annotation.Init;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.INITIALIZED)
public class AlchemyCommandLoader {
	
	public static final AlchemyCommandClient
			c_run = new CommandCRun();
	
	public static final AlchemyCommandServer
			s_run = new CommandSRun();

}
