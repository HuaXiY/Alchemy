package index.alchemy.interacting;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Premise;
import index.alchemy.core.AlchemyDLCLoader;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Loading
@Init(state = ModState.CONSTRUCTED)
public class PremiseCheck {
	
	public static final List<String> premises = new LinkedList<String>();
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Premise premise = clazz.getAnnotation(Premise.class);
		if (premise != null)
			premises.addAll(Arrays.asList(premise.value()));
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (String modid : premises)
			if (!Loader.isModLoaded(modid) && !AlchemyDLCLoader.isDLCLoaded(modid))
				onMiss(modid);
	}
	
	public static void onMiss(String modid) {
		AlchemyRuntimeException.onException(new RuntimeException("Could not find a premise mod or dlc: " + modid));
	}

}