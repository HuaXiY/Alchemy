package index.alchemy.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Omega
public class AlchemyRegistry {
	
	private static final ArrayList<IAlchemyRecipe> alchemys = Lists.newArrayList();
	
	public static void registerAlchemyRecipe(IAlchemyRecipe recipe) {
		AlchemyModLoader.checkState();
		if (recipe.getAlchemyName() == null)
			AlchemyRuntimeException.onException(new NullPointerException(
					"index.alchemy.api.Alchemy.registerAlchemyRecipe, name is null"));
		if (!alchemys.contains(recipe))
			alchemys.add(recipe);
		else
			AlchemyRuntimeException.onException(new RuntimeException(
					"index.alchemy.api.Alchemy.registerAlchemyRecipe, recipe was added before this, " + recipe.getAlchemyName()));
	}
	
	public static void removeAlchemyRecipe(IAlchemyRecipe recipe) {
		AlchemyModLoader.checkState();
		alchemys.remove(recipe);
	}
	
	public static Stream<IAlchemyRecipe> stream() {
		return alchemys.stream();
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(String name) {
		return findRecipe(new ResourceLocation(name));
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(ResourceLocation name) {
		for (IAlchemyRecipe recipe : alchemys)
			if (recipe.getAlchemyName().equals(name))
				return recipe;
		return null;
	}
	
	@Nullable
	public static IAlchemyRecipe findRecipe(List<ItemStack> materials) {
		List<ItemStack> copy = null;
		alchemy: for (IAlchemyRecipe recipe : alchemys) {
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