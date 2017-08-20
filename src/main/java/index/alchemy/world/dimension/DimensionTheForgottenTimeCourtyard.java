package index.alchemy.world.dimension;

import index.alchemy.api.annotation.Dimension;
import index.alchemy.api.annotation.Hook;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.Entity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.gen.IChunkGenerator;

@Alpha
@Hook.Provider
@Dimension(name = "the_forgotten_time_courtyard", load = true)
public class DimensionTheForgottenTimeCourtyard extends AlchemyDimension {
	
	public static final DimensionType type = null;
	
	@Hook("net.minecraft.world.Teleporter#func_180266_a")
	public static Hook.Result placeInPortal(Teleporter teleporter, Entity entity, float rotationYaw) {
		if (teleporter.world.provider.getDimensionType() == type) {
			entity.setLocationAndAngles(0, 255, 0, entity.rotationYaw, 0.0F);
            entity.motionX = 0.0D;
            entity.motionY = 2.0D;
            entity.motionZ = 0.0D;
            return Hook.Result.NULL;
		}
		return Hook.Result.VOID;
	}
	
	@Override
	public float getCloudHeight() {
		return -1;
	}
	
	@Override
	public int getMoonPhase(long worldTime) {
		return 7;
	}
	
	@Override
	public long getWorldTime() {
		return 0;
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderTheForgottenTimeCourtyard(world, world.getSeed());
	}
	
}