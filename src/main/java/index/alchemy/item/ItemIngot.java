package index.alchemy.item;

import net.minecraft.item.ItemStack;
import index.alchemy.api.IOreDictionary;
import index.alchemy.util.Tool;

public class ItemIngot extends AlchemyItemColor implements IOreDictionary {
	
	@Override
	public String getNameInOreDictionary() {
		return Tool._ToUpper(getRegistryName().getResourcePath());
	}
	
	@Override
	public ItemStack getItemStackInOreDictionary() {
		return new ItemStack(this);
	}
	
	public ItemIngot(String name, int color) {
		super(name, "ingot", color);
	}

}