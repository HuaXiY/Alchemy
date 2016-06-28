package index.alchemy.config;

import index.alchemy.api.annotation.Config;

public class AlchemyConfig {
	
	private static final String CATEGORY_NETWORK ="network";
	
	@Config(category = CATEGORY_NETWORK, comment = "Can hear the sound of the range.")
	private static int sound_range = 32;
	
	public static int getSoundRange() {
		return sound_range;
	}
	
	@Config(category = CATEGORY_NETWORK, comment = "Can see the particle of the range.")
	private static int particle_range = 32;
	
	public static int getParticleRange() {
		return particle_range;
	}
	
	@Config(category = CATEGORY_NETWORK, comment = "Can receive the change of the tileentity of the range.")
	private static int tileentity_update_range = 128;
	
	public static int getTileEntityUpdateRange() {
		return tileentity_update_range;
	}

}