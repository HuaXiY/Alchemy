package index.alchemy.sound;

import index.alchemy.api.IRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class AlchemySound extends SoundEvent implements IRegister {
	
	public AlchemySound(ResourceLocation name) {
		super(name);
		setRegistryName(name);
		register();
	}

}
