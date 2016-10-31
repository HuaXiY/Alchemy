package index.alchemy.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IOreDictionary {
	
	String getNameInOreDictionary();
	
	default ItemStack getItemStackInOreDictionary() {
		if (this instanceof Item)
			return new ItemStack((Item) this);
		else if (this instanceof Block)
			return new ItemStack((Block) this);
		else
			throw new AbstractMethodError("net.minecraft.item.ItemStack " + this.getClass().getName() + ".getItemStackInOreDictionary()");
	}

}
