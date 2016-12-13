package index.alchemy.entity.ai;

import static net.minecraft.util.math.MathHelper.*;

import index.alchemy.api.ILocationProvider;
import index.alchemy.util.Always;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

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

}
