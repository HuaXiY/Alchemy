package index.alchemy.util;

import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class CraftingHelper {
	
	public static final void remove(Class<? extends Item> type) {
		remove(i -> type.isInstance(type));
	}
	
	public static final void remove(ItemStack item) {
		remove(i -> InventoryHelper.canMergeItemStack(i, item));
	}
	
	public static final void remove(Predicate<ItemStack> predicate) {
		CraftingManager.getInstance().recipes.removeIf(r -> predicate.test(r.getRecipeOutput()));
	}

}
