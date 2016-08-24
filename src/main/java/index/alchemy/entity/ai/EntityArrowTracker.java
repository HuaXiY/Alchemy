package index.alchemy.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;

import static net.minecraft.util.math.MathHelper.*;

public class EntityArrowTracker {
	
	public static void track(EntityArrow arrow, Entity target) {
		double dx = target.posX - arrow.posX;
        double dy = target.getEntityBoundingBox().minY + target.height / 3 - arrow.posY;
        double dz = target.posZ - arrow.posZ;
        double dr = sqrt_double(dx * dx + dz * dz);
        arrow.setThrowableHeading(dx, dy + dr * 0.3, dz, 2, 2);
	}

}
