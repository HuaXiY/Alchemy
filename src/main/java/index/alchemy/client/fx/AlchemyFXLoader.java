package index.alchemy.client.fx;

import index.alchemy.annotation.Init;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyFXLoader {
	
	public static final String TYPE = "FX";
	
	public static void init() {
		for (Class<?> clazz : AlchemyModLoader.instance_map.get(TYPE))
			try {
				clazz.newInstance();
			} catch (Exception e) {
				AlchemyRuntimeExcption.onExcption(e);
			}
	}

}
