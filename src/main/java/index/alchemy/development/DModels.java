package index.alchemy.development;

import java.io.File;
import java.io.IOException;

import index.alchemy.api.IResourceLocation;
import index.alchemy.api.annotation.DInit;
import index.alchemy.core.Constants;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

@DInit
public class DModels {
	
	public static final String SUFFIX = ".json";
		
	private static final String models_dir = DMain.resources + "/models",
			default_item_json = Tool.readSafe(new File(models_dir + "/item", Constants.MC_VERSION + SUFFIX)),
			default_block_json = Tool.readSafe(new File(models_dir + "/block", Constants.MC_VERSION + SUFFIX));
	
	public static void init() {}
	
	public static void init(Object obj) {
		if (obj instanceof Item)
			init((Item) obj);
		else if (obj instanceof Block)
			init((Block) obj);
	}
	
	public static void init(Item item) {
		ResourceLocation name = item instanceof IResourceLocation ? ((IResourceLocation) item).getResourceLocation() : 
			item.getRegistryName();
		if (name == null)
			return;
		File file = new File(models_dir + "/item", name.getResourcePath() + SUFFIX);
		if (!file.exists())
			try {
				Tool.save(file, default_item_json.replace("${name}", name.getResourcePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static void init(Block block) {
		ResourceLocation name = block instanceof IResourceLocation ? ((IResourceLocation) block).getResourceLocation() : 
			block.getRegistryName();
		if (name == null)
			return;
		File file = new File(models_dir + "/block", name.getResourcePath() + SUFFIX);
		if (!file.exists())
			try {
				Tool.save(file, default_block_json.replace("${name}", name.getResourcePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
