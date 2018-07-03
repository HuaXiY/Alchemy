package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IMaterialConsumer {
	
	/*
	 * if (has Material) return true & items remove material
	 * else return false;
	 */
	boolean treatmentMaterial(List<ItemStack> items);

}