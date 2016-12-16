package index.alchemy.command;

import index.alchemy.api.annotation.Init;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.INITIALIZED)
public class AlchemyCommandLoader {
	
	public static final AlchemyCommandClient
			c_run = new CommandCRun();
	
	public static final AlchemyCommandServer
			s_run = new CommandSRun();

}
