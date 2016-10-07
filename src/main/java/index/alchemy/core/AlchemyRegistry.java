package index.alchemy.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	public static IAlchemyRecipe findRecipe(ResourceLocation name) {
		for (IAlchemyRecipe recipe : ALCHEMY_LIST)
			if (recipe.equals(name))
				return recipe;
		return null;
	}
	
	/*
	 * Use A COPY(List<ItemStack> materials) to invoke this method
	 * This method may change the content of the collection
	 * 
	 * if (return != null) materials -> residues material
	 */
	@Nullable
	public static ItemStack findResult(List<ItemStack> materials) {
		List<ItemStack> copy = null;
		boolean change = true;
		alchemy: for (IAlchemyRecipe recipe : ALCHEMY_LIST) {
			if (change) {
				copy = new LinkedList<ItemStack>(materials);
				change = false;
			}
			for (IMaterialConsumer consumer : recipe.getAlchemyMaterials())
				if (consumer.treatmentMaterial(copy))
					change = true;
				else
					continue alchemy;
			materials.clear();
			materials.addAll(copy);
			return recipe.getAlchemyResult();
		}
		return null;
	}
	
}