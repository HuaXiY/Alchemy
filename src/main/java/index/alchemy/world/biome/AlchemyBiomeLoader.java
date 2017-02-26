package index.alchemy.world.biome;

import index.alchemy.api.annotation.Init;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.INITIALIZED)
public class AlchemyBiomeLoader {
	
	public static final AlchemyBiome
			time = new BiomeGenTime(),
			dragon_island = new BiomeGenDragonIsland();
	
}
