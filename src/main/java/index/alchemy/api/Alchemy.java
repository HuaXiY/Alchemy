package index.alchemy.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class Alchemy {
	
	private static final ArrayList<IAlchemyRecipe> ALCHEMY_LIST = new ArrayList<IAlchemyRecipe>();
	
	public static void registerAlchemyRecipe(IAlchemyRecipe recipe) {
		AlchemyModLoader.checkState();
		if (recipe.getAlchemyName() == null)
			AlchemyRuntimeExcption.onExcption(new RuntimeException("index.alchemy.api.Alchemy.registerAlchemyRecipe, name is null"));
		for (ItemStack item : recipe.getMaterial())
			if (item == null)
				AlchemyRuntimeExcption.onExcption(new RuntimeException("index.alchemy.api.Alchemy.registerAlchemyRecipe, recipe has null, " + recipe.getAlchemyName()));
		if (!ALCHEMY_LIST.contains(recipe))
			ALCHEMY_LIST.add(recipe);
		else
			AlchemyRuntimeExcption.onExcption(new RuntimeException("index.alchemy.api.Alchemy.registerAlchemyRecipe, recipe was added before this, " + recipe.getAlchemyName()));
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
	
	@Nullable
	public static ItemStack findResult(List<ItemStack> list) {
		list: for (int i = 0, len = ALCHEMY_LIST.size(); i < len; i++) {
			IAlchemyRecipe recipe = ALCHEMY_LIST.get(i);
			if (recipe.getMaterial().size() == list.size()) {
				Iterator<ItemStack> r = recipe.getMaterial().iterator(), l = list.iterator();
				while (r.hasNext()) {
					ItemStack ri = r.next(), li = l.next();
					if (li == null)
						AlchemyRuntimeExcption.onExcption(new RuntimeException("index.alchemy.api.Alchemy.findResult, input has null"));
					if (!ItemStack.areItemStacksEqual(ri, li))
						continue list;
				}
				return recipe.getResult().copy();
			}
		}
		return null;
	}

}
