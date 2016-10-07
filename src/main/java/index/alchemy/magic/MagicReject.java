package index.alchemy.magic;

import index.alchemy.entity.ai.EntityTrackTracker;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;

public class MagicReject extends AlchemyMagic {
	
	public MagicReject() {
		setStrength(3F);
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		new EntityTrackTracker(Always.generateLocationProvider(src, src.height / 2), -strength * amplify).update(living, living.height / 2);
	}

}