package index.alchemy.development;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.IResourceLocation;
import index.alchemy.util.Tool;

import java.io.File;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

@DInit
public class DBlockState {
	
	public static final String SUFFIX = ".json";
	
	public static String states_dir = DMain.resources + "/blockstates", default_json = "";
	static {
		try {
			default_json = Tool.read(new File(states_dir, Constants.MC_VERSION + SUFFIX));
		} catch (IOException e) {
			AlchemyModLoader.logger.warn("Can't load: " + Constants.MC_VERSION + SUFFIX);
		}
	}
	
	public static void init() {}
	
	public static void init(Object obj) {
		if (obj instanceof Block) {
			ResourceLocation name = obj instanceof IResourceLocation ? ((IResourceLocation) obj).getResourceLocation() : 
				((Block) obj).getRegistryName();
			File file = new File(states_dir, name.getResourcePath() + SUFFIX);
			if (!file.exists())
				try {
					Tool.save(file, default_json.replace("${name}", name.getResourcePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
