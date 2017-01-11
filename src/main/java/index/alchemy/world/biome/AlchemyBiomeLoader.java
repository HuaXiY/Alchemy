package index.alchemy.world.biome;

import index.alchemy.api.annotation.Init;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.INITIALIZED)
public class AlchemyBiomeLoader {
	
	public static final AlchemyBiome
			dragon_island = new BiomeGenDragonIsland();
	
	static { Tool.where(); }
	
}
