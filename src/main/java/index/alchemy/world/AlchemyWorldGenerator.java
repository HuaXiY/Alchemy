package index.alchemy.world;

import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class AlchemyWorldGenerator implements IWorldGenerator {
	
	protected AlchemyWorldGenerator(int level) {
		GameRegistry.registerWorldGenerator(this, level);	
	}
	
}
