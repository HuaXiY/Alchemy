package index.alchemy.magic;

import index.alchemy.api.Alway;
import index.alchemy.entity.ai.EntityTrackTracker;
import net.minecraft.entity.EntityLivingBase;

public class MagicTow extends AlchemyMagic {
	
	public MagicTow() {
		setStrength(3F);
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		new EntityTrackTracker(Alway.generateLocationProvider(src, src.height / 2), strength * amplify).update(living, living.height / 2);
	}

}