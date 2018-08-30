package index.alchemy.entity.ai;

import static net.minecraft.util.math.MathHelper.*;

import index.alchemy.api.ILocationProvider;
import index.alchemy.util.Always;
import index.project.version.annotation.Beta;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Beta
public class EntityTrackTracker {
	
	private ILocationProvider location;
	private double acceleration;
	
	public EntityTrackTracker(ILocationProvider location, double acceleration) {
		this.location = location;
		this.acceleration = acceleration;
	}
	
	public void update(Entity tracker, double offsetY) {
		Vec3d src = location.getLocation();
		Vec3d tra = Always.generateLocationProvider(tracker, offsetY).getLocation();
		
		double dx = src.x - tra.x;
		double dy = src.y - tra.y;
		double dz = src.z - tra.z;
		double max = sqrt(dx * dx + dy * dy + dz * dz);
		
		tracker.motionX += dx / max * acceleration;
		tracker.motionY += dy / max * acceleration;
		tracker.motionZ += dz / max * acceleration;
		
		tracker.prevRotationYaw = tracker.rotationYaw = (float) (atan2(dx, dz) * (180D / Math.PI));
		tracker.prevRotationPitch = tracker.rotationPitch = (float) atan2(dy, sqrt(dx * dx + dz * dz) * (180D / Math.PI));
	}

}
