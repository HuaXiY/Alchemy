package index.alchemy.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AABBHelper {
	
	public static final AxisAlignedBB getAABBFromEntity(Entity entity, double r) {
		return new AxisAlignedBB(entity.posX - r, entity.posY - r, entity.posZ - r, entity.posX + r, entity.posY + r, entity.posZ + r);
	}
	
	public static final AxisAlignedBB getAABBFromBlockPos(BlockPos pos, double r) {
		return new AxisAlignedBB(pos.getX() - r, pos.getY() - r, pos.getZ() - r, pos.getX() + r, pos.getY() + r, pos.getZ() + r);
	}
	
}