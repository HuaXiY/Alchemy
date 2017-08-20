package index.alchemy.api;

import index.alchemy.api.annotation.Hook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Hook.Provider
@FunctionalInterface
public interface IMaterialContainer {
	
	boolean isMaterialInBB(World world, BlockPos pos, Material material);
	
	@Hook("net.minecraft.world.World#func_72875_a")
	static Hook.Result isMaterialInBB(World world, AxisAlignedBB bb, Material material) {
		int minX = MathHelper.floor(bb.minX);
		int maxX = MathHelper.ceil(bb.maxX);
		int minY = MathHelper.floor(bb.minY);
		int maxY = MathHelper.ceil(bb.maxY);
		int minZ = MathHelper.floor(bb.minZ);
		int maxZ = MathHelper.ceil(bb.maxZ);
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++)
				for (int z = minZ; z < maxZ; z++) {
					Block block = world.getBlockState(pos.setPos(x, y, z)).getBlock();
					if (block instanceof IMaterialContainer && ((IMaterialContainer) block).isMaterialInBB(world, pos, material)) {
						pos.release();
						return Hook.Result.TRUE;
					}
				}
		pos.release();
		return Hook.Result.VOID;
	}

}
