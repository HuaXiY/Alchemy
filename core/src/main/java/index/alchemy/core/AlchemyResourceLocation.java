package index.alchemy.core;

import index.project.version.annotation.Omega;

import net.minecraft.util.ResourceLocation;

@Omega
public class AlchemyResourceLocation extends ResourceLocation {
    
    public AlchemyResourceLocation(String name) {
        super(AlchemyConstants.MOD_ID.toLowerCase(), name);
    }
    
}
