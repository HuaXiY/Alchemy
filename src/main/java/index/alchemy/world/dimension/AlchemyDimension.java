package index.alchemy.world.dimension;

import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

@Omega
public abstract class AlchemyDimension extends WorldProvider {
	
	protected double movement = 1;
	
	@Override
	public DimensionType getDimensionType() {
		return AlchemyDimensionType.findDimensionType(getClass());
	}
	
	@Override
	public String getSaveFolder() {
		return "Alchemy-DIM" + getDimension();
	}
	
	@Override
	public double getMovementFactor() {
		return movement;
	}
	
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		return getDimension();
	}

}
