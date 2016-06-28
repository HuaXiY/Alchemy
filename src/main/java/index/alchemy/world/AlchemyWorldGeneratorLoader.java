package index.alchemy.world;

import index.alchemy.api.annotation.Init;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED, enable = false)
public class AlchemyWorldGeneratorLoader {
	
	public static final AlchemyWorldGenerator
			red_dragon_nest = new WorldGeneratorRedDragonNest();
	
}