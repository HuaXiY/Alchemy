package index.alchemy.entity.ai;

import index.alchemy.api.Always;
import index.alchemy.api.ILocationProvider;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Test;
import index.alchemy.util.AABBHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import static net.minecraft.util.math.MathHelper.*;

public class EntityTrackTracker {
	
	private ILocationProvider location;
	private double acceleration, deceleration_distance;
	
	public EntityTrackTracker(ILocationProvider location, double acceleration) {
		this.location = location;
		this.acceleration = acceleration;
	}
	
	public void update(Entity tracker, double offsetY) {
		Vec3d src = location.getLocation();
		Vec3d tra = Always.generateLocationProvider(tracker, offsetY).getLocation();
		
		double dx = src.xCoord - tra.xCoord;
		double dy = src.yCoord - tra.yCoord;
		double dz = src.zCoord - tra.zCoord;
		double max = sqrt_double(dx * dx + dy * dy + dz * dz);
		
		tracker.motionX += dx / max * acceleration;
		tracker.motionY += dy / max * acceleration;
		tracker.motionZ += dz / max * acceleration;
		
		tracker.prevRotationYaw = tracker.rotationYaw = (float) (atan2(dx, dz) * (180D / Math.PI));
		tracker.prevRotationPitch = tracker.rotationPitch = (float) atan2(dy, sqrt_double(dx * dx + dz * dz) * (180D / Math.PI));
	}
	
	@Test
	@Init(state = ModState.POSTINITIALIZED)
	public static class TEntityTrackTracker implements IPlayerTickable {
		
		@Override
		public Side getSide() {
			return null;
		}

		@Override
		public void onTick(EntityPlayer player, Phase phase) {
			if (phase == Phase.START)
				for (EntityArrow throwable : player.worldObj.getEntitiesWithinAABB(EntityArrow.class, 
						AABBHelper.getAABBFromEntity(player, 2).addCoord(0, 1, 0))) {
					if (throwable.shootingEntity != null && throwable.shootingEntity != player) {
						throwable.motionX = 0;
						throwable.motionY = 0;
						throwable.motionZ = 0;
						new EntityTrackTracker(Always.generateLocationProvider(throwable.shootingEntity, 2), 5).update(throwable, 1);
					}
				}
		}
		
	}

}
