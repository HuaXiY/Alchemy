package index.alchemy.world.dimension;

import index.alchemy.api.annotation.Init;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyDimensionLoader {
	
	public static void init() {
		DimensionManager.registerDimension(10, DimensionTheForgottenTimeCourtyard.type);
	}

}