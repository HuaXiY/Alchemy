package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IAlchemyRecipe {
	
	public ItemStack getResult();
	
	public List<ItemStack> getMaterial();

}
