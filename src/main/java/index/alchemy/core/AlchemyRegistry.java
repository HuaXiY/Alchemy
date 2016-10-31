package index.alchemy.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import index.alchemy.api.IAlchemyRecipe;
import index.alchemy.api.IMaterialConsumer;
import index.alchemy.core.debug.AlchemyRuntimeException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AlchemyRegistry {
	
	private static final ArrayList<IAlchemyRecipe> ALCHEMY_LIST = new ArrayList<IAlchemyRecipe>();
	
	public static void registerAlchemyRecipe(IAlchemyRecipe recipe) {
		AlchemyModLoader.checkState();
		if (recipe.getAlchemyName() == null)
			AlchemyRuntimeException.onException(new NullPointerException(
					"index.alchemy.api.Alchemy.registerAlchemyRecipe, name is null"));
		if (!ALCHEMY_LIST.contains(recipe))
			ALCHEMY_LIST.add(recipe);
		else
			AlchemyRuntimeException.onException(new RuntimeException(
					"index.alchemy.api.Alchemy.registerAlchemyRecipe, recipe was added before this, " + recipe.getAlchemyName()));
	}
	
	public static void removeAlchemyRecipe(IAlchemyRecipe recipe) {
		AlchemyModLoader.checkState();
		ALCHEMY_LIST.remove(recipe);
	}
	
	public static List<IAlchemyRecipe> copy() {
		return (List<IAlchemyRecipe>) ALCHEMY_LIST.clone();
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(String name) {
		return findRecipe(new ResourceLocation(name));
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(ResourceLocation name) {
		for (IAlchemyRecipe recipe : ALCHEMY_LIST)
			if (recipe.getAlchemyName().equals(name))
				return recipe;
		return null;
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(List<ItemStack> materials) {
		List<ItemStack> copy = null;
		alchemy: for (IAlchemyRecipe recipe : ALCHEMY_LIST) {
			copy = materials.stream().map(ItemStack::copy).collect(Collectors.toList());
			for (IMaterialConsumer consumer : recipe.getAlchemyMaterials())
				if (!consumer.treatmentMaterial(copy))
					continue alchemy;
			if (!copy.isEmpty())
				continue alchemy;
			return recipe;
		}
		return null;
	}
	
}