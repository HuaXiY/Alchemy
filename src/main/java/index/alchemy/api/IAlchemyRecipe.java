package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IAlchemyRecipe {
	
	public ResourceLocation getAlchemyName();
	
	public ItemStack getAlchemyResult();
	
	public List<IMaterialConsumer> getAlchemyMaterial();

}