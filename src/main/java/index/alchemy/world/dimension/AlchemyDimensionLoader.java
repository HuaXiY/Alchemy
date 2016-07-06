package index.alchemy.world.dimension;

import index.alchemy.api.Alway;
import index.alchemy.api.annotation.Init;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyDimensionLoader {
	
	public static void init() {
		System.out.println(DimensionType.values().length);
		for (DimensionType type : DimensionType.values())
			System.out.println(type.getName() + " - " + type.getId());
		System.out.println("---");
		System.out.println(DimensionTheForgottenTimeCourtyard.type.getId());
		DimensionManager.registerDimension(10, DimensionTheForgottenTimeCourtyard.type);
		//DimensionManager.createProviderFor(10);
	}

}