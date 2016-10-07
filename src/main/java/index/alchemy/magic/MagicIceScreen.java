package index.alchemy.magic;

import index.alchemy.block.AlchemyBlockLoader;
import index.alchemy.interacting.Elemix;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagicIceScreen extends AlchemyMagic {
	
	@Override
	public boolean hasStrength() {
		return false;
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		int posX = (int) living.posX, posY = (int) living.posY, posZ = (int) living.posZ;
		World world = living.worldObj;
		if (Always.isServer()) {
			for (int x = -1; x < 2; x++) 
				for (int y = -1; y < 3; y++) 
					for (int z = -1; z < 2; z++) {
						if ((!(x == 0 && z == 0) || (y == -1 || y == 2)) &&
								Elemix.blockCanToIce(world.getBlockState(new BlockPos(posX + x, posY + y, posZ + z)).getBlock())) {
							world.setBlockState(new BlockPos(posX + x, posY + y, posZ + z), AlchemyBlockLoader.ice_temp.getDefaultState());
						}
					}
		}
		living.fallDistance = 0;
		living.setPosition(posX + 0.5, living.posY > posY ? posY + 1 : posY, posZ + 0.5);
		living.motionX = living.motionZ = living.motionY = 0;
	}

}