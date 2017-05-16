package index.alchemy.util;

import java.util.function.Predicate;

import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Omega
public interface CraftingHelper {
	
	static void remove(Class<? extends Item> type) {
		remove(i -> i != null && type.isInstance(i.getItem()));
	}
	
	static void remove(Item item) {
		remove(i -> i != null && i.getItem() == item);
	}
	
	static void remove(ItemStack item) {
		remove(i -> InventoryHelper.canMergeItemStack(i, item));
	}
	
	static void remove(Predicate<ItemStack> predicate) {
		CraftingManager.getInstance().recipes.removeIf(r -> predicate.test(r.getRecipeOutput()));
	}
	
	static void add(Item item, Object... args) {
		add(new ItemStack(item), args);
	}
	
	static void add(Block block, Object... args) {
		add(new ItemStack(block), args);
	}
	
	static void add(ItemStack item, Object... args) {
		GameRegistry.addRecipe(item, args);
	}
	
	static void add(IRecipe recipe) {
		GameRegistry.addRecipe(recipe);
	}
	
	static void addShapeless(Item item, Object... args) {
		add(new ItemStack(item), args);
	}
	
	static void addShapeless(Block block, Object... args) {
		add(new ItemStack(block), args);
	}
	
	static void addShapeless(ItemStack item, Object... args) {
		GameRegistry.addShapelessRecipe(item, args);
	}

}
