package index.alchemy.core;

import java.util.Map;

import index.project.version.annotation.Beta;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

@Beta
public class AlchemySetup implements IFMLCallHook {

	@Override
	public Void call() throws Exception {
		AlchemyDLCLoader.setup();
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) { }

}
