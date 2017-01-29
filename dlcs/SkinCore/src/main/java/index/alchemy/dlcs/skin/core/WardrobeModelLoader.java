package index.alchemy.dlcs.skin.core;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import index.alchemy.interacting.WoodType;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;

@Alpha
@SideOnly(Side.CLIENT)
public class WardrobeModelLoader implements ICustomModelLoader {
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) { }
	
	@Alpha
	@Override
	public IModel loadModel(ResourceLocation resourceLocation) throws Exception {
		try {
			ModelResourceLocation modelLocation;
			if (resourceLocation instanceof ModelResourceLocation)
				modelLocation = (ModelResourceLocation) resourceLocation;
			else
				return null;
			if (!modelLocation.getVariant().equals("inventory") && !modelLocation.getResourcePath().endsWith("_loc")) {
				List<Variant> list = Lists.newLinkedList();
				ModelRotation rotation = ModelRotation.X0_Y0;
				switch (Tool.get(modelLocation.getVariant(), "facing=([a-z]*)")) {
					case "east":
						rotation = ModelRotation.X0_Y270;
						break;
					case "north":
						rotation = ModelRotation.X0_Y180;
						break;
					case "west":
						rotation = ModelRotation.X0_Y90;
						break;
				}
				list.add(new Variant(new ModelResourceLocation(modelLocation.getResourceDomain() + ":" +
						modelLocation.getResourcePath() + "_loc", modelLocation.getVariant()),
						rotation, false, 1));
				VariantList variantList = new VariantList(list);
				return new ModelLoader.WeightedRandomModel(modelLocation, variantList);
			}
			modelLocation = new ModelResourceLocation(modelLocation.getResourceDomain() + 
					modelLocation.getResourcePath().replace("_loc", ""), modelLocation.getVariant());
			String name = modelLocation.getVariant().contains("part=head") ? "head" : "foot";
			String rely = Tool.isEmptyOr(Tool.get(modelLocation.getVariant(), "rely=([a-z]*)"), "null");
			try (IResource resource = Minecraft.getMinecraft().getResourceManager()
						.getResource(new ResourceLocation("skin:models/wardrobe_" + rely + "_" + name + ".json"))) {
				String json = Joiner.on('\n').join(IOUtils.readLines(resource.getInputStream(), Charsets.UTF_8));
				try (Reader reader = new StringReader(WoodType.conversion.apply(json, Tool.get(
						modelLocation.getResourcePath(), "_(.*?_T_.*?_T_[0-9]*_T_.*?_T_.*?_T_[0-9]*)")))) {
					ModelBlock modelBlock = ModelBlock.deserialize(reader);
					return modelLocation.getVariant().equals("inventory") ? new ItemLayerModel(modelBlock) :
						ModelLoader.VanillaLoader.INSTANCE.getLoader().new VanillaModelWrapper(modelLocation,
								modelBlock, false, ModelBlockAnimation.defaultModelBlockAnimation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation.getResourceDomain().equals(MOD_ID) && modelLocation.getResourcePath().startsWith("wardrobe_");
	}

}
