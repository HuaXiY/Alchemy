package index.alchemy.dlcs.volcanic_island.core;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Premise;
import index.alchemy.core.AlchemyConstants;
import net.minecraftforge.fml.common.LoaderState.ModState;

import static index.alchemy.dlcs.volcanic_island.core.DLCVolcanicIsland.*;

@Premise(AlchemyConstants.MOD_ID)
@Init(state = ModState.CONSTRUCTED)
@DLC(id = MOD_ID, name = MOD_NAME, version = MOD_VERSION, mcVersion = "1.10.2")
public class DLCVolcanicIsland {
	
	public static final String 
			MOD_ID = "volcanic_island",
			MOD_NAME = "VolcanicIsland",
			MOD_VERSION = "0.0.1";
	
	public static void init() {
		System.out.println(MOD_NAME + " - Init");
	}

}
