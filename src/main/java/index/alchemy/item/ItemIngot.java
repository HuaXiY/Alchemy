package index.alchemy.item;

import index.alchemy.api.IOreDictionary;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;

@Alpha
public class ItemIngot extends AlchemyItemColor implements IOreDictionary {
	
	@Override
	public String getNameInOreDictionary() {
		return Tool._ToUpper(getRegistryName().getResourcePath());
	}
	
	public ItemIngot(String name, int color) {
		super(name, "ingot", color);
	}

}