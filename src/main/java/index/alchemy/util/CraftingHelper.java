package index.alchemy.util;

import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public interface CraftingHelper {
	
	static void remove(Class<? extends Item> type) {
		remove(i -> i != null && type.isInstance(i.getItem()));
	}
	
	static void remove(ItemStack item) {
		remove(i -> InventoryHelper.canMergeItemStack(i, item));
	}
	
	static void remove(Predicate<ItemStack> predicate) {
		CraftingManager.getInstance().recipes.removeIf(r -> predicate.test(r.getRecipeOutput()));
	}

}
