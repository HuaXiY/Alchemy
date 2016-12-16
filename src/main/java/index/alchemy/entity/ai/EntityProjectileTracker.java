package index.alchemy.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;

import static net.minecraft.util.math.MathHelper.*;

import index.project.version.annotation.Beta;

@Beta
public class EntityProjectileTracker {
	
	public static <T extends Entity & IProjectile> void track(T entity, Entity target) {
		double dx = target.posX - entity.posX;
		double dy = target.getEntityBoundingBox().minY + target.height / 3 - entity.posY;
		double dz = target.posZ - entity.posZ;
		double dr = sqrt_double(dx * dx + dz * dz);
		entity.setThrowableHeading(dx, dy + dr * 0.2, dz, 1.6F, 2);
	}

}
