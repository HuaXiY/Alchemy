package index.alchemy.world;

import index.alchemy.api.annotation.Init;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED, enable = false)
public class AlchemyWorldGeneratorLoader {
	
	public static final AlchemyWorldGenerator
			red_dragon_nest = new WorldGeneratorRedDragonNest();
	
}