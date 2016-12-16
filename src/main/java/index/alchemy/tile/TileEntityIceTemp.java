package index.alchemy.tile;

import index.project.version.annotation.Omega;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

@Omega
public class TileEntityIceTemp extends TileEntity implements ITickable {
	
	public int time = 80;
	
	@Override
	public void update() {
		if (--time == 0) {
			worldObj.removeTileEntity(pos);
			worldObj.setBlockState(pos, Blocks.AIR.getDefaultState());
			worldObj.notifyNeighborsOfStateChange(pos, blockType);
			worldObj.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK,
					SoundCategory.NEUTRAL, 1.0F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
			worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, pos.getX(), pos.getY(), pos.getZ(), .5, .5, .5);
		}
	}
	
}