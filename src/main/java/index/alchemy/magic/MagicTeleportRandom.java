package index.alchemy.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class MagicTeleportRandom extends AlchemyMagic {
	
	protected int range = 100000;

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		int x = living.rand.nextInt(range), z = living.rand.nextInt((int) (range * amplify));
		living.setPositionAndUpdate(x, living.worldObj.func_189649_b(x, z), z);
	}

}