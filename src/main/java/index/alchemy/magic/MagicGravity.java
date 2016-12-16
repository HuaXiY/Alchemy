package index.alchemy.magic;

import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLivingBase;

@Alpha
public class MagicGravity extends AlchemyMagic {
	
	public MagicGravity() {
		setStrength(2F);
	}
	
	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		living.motionY -= strength;
	}

}
