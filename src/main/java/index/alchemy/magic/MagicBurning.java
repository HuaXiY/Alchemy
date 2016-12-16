package index.alchemy.magic;

import index.alchemy.util.Always;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLivingBase;

@Alpha
public class MagicBurning extends AlchemyMagic {
	
	public MagicBurning() {
		setStrength(8F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Always.isServer())
			living.fire = (int) (strength * amplify);
	}

}