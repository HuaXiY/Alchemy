package index.alchemy.util;

import index.project.version.annotation.Omega;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Omega
public interface AABBHelper {
	
	static AxisAlignedBB getAABBFromEntity(Entity entity, double r) {
		return new AxisAlignedBB(entity.posX - r, entity.posY - r, entity.posZ - r, entity.posX + r, entity.posY + r, entity.posZ + r);
	}
	
	static AxisAlignedBB getAABBFromBlockPos(BlockPos pos, double r) {
		return new AxisAlignedBB(pos.getX() - r, pos.getY() - r, pos.getZ() - r, pos.getX() + r, pos.getY() + r, pos.getZ() + r);
	}
	
}