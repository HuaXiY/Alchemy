package index.alchemy.interacting;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.CONSTRUCTED)
public class Premise {
	
	public static final List<String> MODID_LIST = new LinkedList<String>();
	static {
		MODID_LIST.add("BiomesOPlenty");
		MODID_LIST.add("Baubles");
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (String modid : MODID_LIST)
			if (!Loader.isModLoaded(modid))
				onMiss(modid);
	}
	
	public static void onMiss(String modid) {
		AlchemyRuntimeException.onException(new RuntimeException("Could not find a prerequisite mod: " + modid));
	}

}
