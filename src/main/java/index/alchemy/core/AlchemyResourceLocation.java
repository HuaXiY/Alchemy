package index.alchemy.core;

import net.minecraft.util.ResourceLocation;

public class AlchemyResourceLocation extends ResourceLocation {

	public AlchemyResourceLocation(String name) {
		super(AlchemyConstants.MOD_ID.toLowerCase(), name);
	}
	
}
