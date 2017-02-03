package index.alchemy.api;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import index.alchemy.util.Tool;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public interface IItemPotion {
	
	Consumer<ItemTooltipEvent> callback = null;
	
	class EffectsMap implements Function<ItemStack, List<PotionEffect>> {
		
		public Map<Integer, List<PotionEffect>> mapping = Maps.newHashMap();
		public Function<ItemStack, List<PotionEffect>> handle = i -> Lists.newArrayList();

		@Nullable
		@Override
		public List<PotionEffect> apply(ItemStack key) {
			return Tool.isNullOr(mapping.get(key.getMetadata()), () -> handle.apply(key));
		}
		
	}
	
	List<PotionEffect> getEffects(ItemStack item);
	
	void setEffects(int meta, List<PotionEffect> effects);
	
	void setEffects(Function<ItemStack, List<PotionEffect>> handle);
	
	void clearEffects();
	
}
