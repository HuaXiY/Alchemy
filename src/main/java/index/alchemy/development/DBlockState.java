package index.alchemy.development;

import java.io.File;
import java.io.IOException;

import index.alchemy.api.IResourceLocation;
import index.alchemy.api.annotation.DInit;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@DInit
@SideOnly(Side.CLIENT)
public class DBlockState {
	
	public static final String SUFFIX = ".json";
	
	private static final String states_dir = DMain.resources + "/blockstates", default_json = Tool.readSafe(new File(states_dir, AlchemyConstants.MC_VERSION + SUFFIX));
	
	public static void init() {}
	
	public static void init(Object obj) {
		if (obj instanceof Block) {
			ResourceLocation name = obj instanceof IResourceLocation ? ((IResourceLocation) obj).getResourceLocation() : 
				((Block) obj).getRegistryName();
			if (name == null)
				return;
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
