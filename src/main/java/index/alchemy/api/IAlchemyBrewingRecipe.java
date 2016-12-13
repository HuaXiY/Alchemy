package index.alchemy.api;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public interface IAlchemyBrewingRecipe extends IBrewingRecipe {
	
	@Override
	default boolean isInput(ItemStack input) {
		return input.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(input) == PotionTypes.AWKWARD;
	}
	
	@Override
	default ItemStack getOutput(ItemStack input, ItemStack ingredient) {
		return isInput(input) && isIngredient(ingredient) ? getOutput() : null;
	}
	
	ItemStack getOutput();

}
