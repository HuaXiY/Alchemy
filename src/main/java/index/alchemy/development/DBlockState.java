package index.alchemy.development;

import java.io.File;
import java.io.IOException;

import index.alchemy.core.Constants;
import index.alchemy.core.IResourceLocation;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

@DInit
public class DBlockState {
	
	public static final String SUFFIX = ".json";
	
	private static final String states_dir = DMain.resources + "/blockstates", default_json = Tool.readSafe(new File(states_dir, Constants.MC_VERSION + SUFFIX));
	
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
