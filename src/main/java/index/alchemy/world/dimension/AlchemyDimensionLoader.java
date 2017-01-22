package index.alchemy.world.dimension;

import index.alchemy.api.annotation.Init;
import index.project.version.annotation.Alpha;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Alpha
@Init(state = ModState.PREINITIALIZED)
public class AlchemyDimensionLoader {
	
	public static void init() {
		DimensionManager.registerDimension(10, DimensionTheForgottenTimeCourtyard.type);
	}

}