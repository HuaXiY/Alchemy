package index.alchemy.api;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import net.minecraft.item.ItemStack;

public interface IItemThirst {
	
	int getThirst(ItemStack item);
	
	default void setThirst(int thirst) { setThirst(i -> thirst); }
	
	void setThirst(ToIntFunction<ItemStack> handle);
	
	float getHydration(ItemStack item);
	
	default void setHydration(float hydration) { setHydration(i -> hydration); }
	
	void setHydration(Function<ItemStack, Float> handle);

}
