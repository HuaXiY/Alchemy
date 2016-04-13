package index.alchemy.item;

import index.alchemy.core.IOreDictionary;
import index.alchemy.util.Tool;

public class ItemIngot extends AlchemyItemColor implements IOreDictionary {
	
	@Override
	public String getNameInOreDictionary() {
		return Tool._toUp(getRegistryName().getResourcePath());
	}
	
	public ItemIngot(String name, int color) {
		super(name, "ingot", color);
	}

}