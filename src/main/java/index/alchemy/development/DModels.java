package index.alchemy.development;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.IResourceLocation;
import index.alchemy.util.Tool;

import java.io.File;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

@DInit
public class DModels {
	
public static final String SUFFIX = ".json";
	
	public static String models_dir = DMain.resources + "/models", default_item_json = "", default_block_json = "";
	static {
		try {
			default_item_json = Tool.read(new File(models_dir + "/item", Constants.MC_VERSION + SUFFIX));
		} catch (IOException e) {
			AlchemyModLoader.logger.warn("Can't load: " + Constants.MC_VERSION + SUFFIX);
		}
		try {
			default_item_json = Tool.read(new File(models_dir + "block", Constants.MC_VERSION + SUFFIX));
		} catch (IOException e) {
			AlchemyModLoader.logger.warn("Can't load: " + Constants.MC_VERSION + SUFFIX);
		}
	}
	
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
		File file = new File(models_dir + "/block", name.getResourcePath() + SUFFIX);
		if (!file.exists())
			try {
				Tool.save(file, default_block_json.replace("${name}", name.getResourcePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
