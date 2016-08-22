package index.alchemy.world.dimension;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

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
	public String getWelcomeMessage() {
		return "";
	}
	
	@Override
	public String getDepartMessage() {
		return "";
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
