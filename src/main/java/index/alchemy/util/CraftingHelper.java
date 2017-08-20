package index.alchemy.util;

import java.util.function.Predicate;

import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
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
		ForgeHelper.getUnregister(IRecipe.class).unregistryIf((loc, r) -> predicate.test(r.getRecipeOutput()));
	}
	
	static void add(ResourceLocation name, ResourceLocation group, Item item, Object... args) {
		add(name, group, new ItemStack(item), args);
	}
	
	static void add(ResourceLocation name, ResourceLocation group, Block block, Object... args) {
		add(name, group, new ItemStack(block), args);
	}
	
	static void add(ResourceLocation name, ResourceLocation group, ItemStack item, Object... args) {
		GameRegistry.addShapedRecipe(name, group, item, args);
	}
	
	static void add(IRecipe recipe) {
		GameRegistry.register(recipe);
	}
	
	static void addShapeless(ResourceLocation name, ResourceLocation group, Item item, Object... args) {
		add(name, group, new ItemStack(item), args);
	}
	
	static void addShapeless(ResourceLocation name, ResourceLocation group, Block block, Object... args) {
		add(name, group, new ItemStack(block), args);
	}
	
	static void addShapeless(ResourceLocation name, ResourceLocation group, ItemStack item, Ingredient... args) {
		GameRegistry.addShapelessRecipe(name, group, item, args);
	}

}
