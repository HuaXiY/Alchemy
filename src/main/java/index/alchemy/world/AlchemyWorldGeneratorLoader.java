package index.alchemy.world;

import biomesoplenty.common.world.GeneratorRegistry;
import index.alchemy.api.annotation.Generator;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Loading
@Init(state = ModState.PREINITIALIZED)
public class AlchemyWorldGeneratorLoader {
	
//	public static final AlchemyWorldGenerator
//			red_dragon_nest = new WorldGeneratorRedDragonNest();
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Generator generator = clazz.getAnnotation(Generator.class);
		if (generator != null)
			GeneratorRegistry.registerGenerator(generator.identifier(), (Class) clazz, Tool.instance(generator.builder()));
	}
	
}