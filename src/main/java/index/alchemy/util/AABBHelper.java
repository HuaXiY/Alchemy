package index.alchemy.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class AABBHelper {
	
	public static AxisAlignedBB getAABBFromEntity(Entity entity, double r) {
		return new AxisAlignedBB(entity.posX - r, entity.posY - r, entity.posZ - r, entity.posX + r, entity.posY + r, entity.posZ + r);
	}

}
