package index.alchemy.magic;

import index.project.version.annotation.Alpha;
import net.minecraft.entity.EntityLivingBase;

@Alpha
public class MagicTeleportRandom extends AlchemyMagic {
	
	protected int range = 100000;

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		int x = living.rand.nextInt(range), z = living.rand.nextInt((int) (range * amplify));
		living.setPositionAndUpdate(x, living.worldObj.getHeightmapHeight(x, z), z);
	}

}