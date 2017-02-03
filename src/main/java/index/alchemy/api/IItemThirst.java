package index.alchemy.api;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import com.google.common.collect.Maps;

import index.alchemy.util.Tool;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public interface IItemThirst {
	
	Consumer<ItemTooltipEvent> callback = null;
	
	class ThirstMap implements ToIntFunction<ItemStack> {
		
		public Map<Integer, Integer> mapping = Maps.newHashMap();
		public ToIntFunction<ItemStack> handle = i -> 0;

		@Override
		public int applyAsInt(ItemStack key) {
			return Tool.isNullOr(mapping.get(key.getMetadata()), () -> handle.applyAsInt(key));
		}
		
	}
	
	class HydrationMap implements Function<ItemStack, Float> {
		
		public Map<Integer, Float> mapping = Maps.newHashMap();
		public Function<ItemStack, Float> handle = i -> 0F;

		@Override
		public Float apply(ItemStack key) {
			return Tool.isNullOr(mapping.get(key.getMetadata()), () -> handle.apply(key));
		}
		
	}
	
	int getThirst(ItemStack item);
	
	void setThirst(int meta, int thirst);
	
	void setThirst(ToIntFunction<ItemStack> handle);
	
	float getHydration(ItemStack item);
	
	void setHydration(int meta, float hydration);
	
	void setHydration(Function<ItemStack, Float> handle);

}
