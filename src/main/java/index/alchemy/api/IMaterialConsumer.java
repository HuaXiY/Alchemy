package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IMaterialConsumer {
	
	/*
	 * if (has Material) return true & items remove material
	 * else return false;
	 */
	boolean treatmentMaterial(List<ItemStack> items);

}