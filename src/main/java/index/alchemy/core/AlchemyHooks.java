package index.alchemy.core;

import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.annotation.Hook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AlchemyHooks {
	
	@Hook("net.minecraft.world.World#func_72875_a")
	public static Hook.Result isMaterialInBB(World world, AxisAlignedBB bb, Material material) {
		int minX = MathHelper.floor_double(bb.minX);
		int maxX = MathHelper.ceiling_double_int(bb.maxX);
		int minY = MathHelper.floor_double(bb.minY);
		int maxY = MathHelper.ceiling_double_int(bb.maxY);
		int minZ = MathHelper.floor_double(bb.minZ);
		int maxZ = MathHelper.ceiling_double_int(bb.maxZ);
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();

		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++)
				for (int z = minZ; z < maxZ; z++) {
					Block block = world.getBlockState(pos.setPos(x, y, z)).getBlock();
					if (block instanceof IMaterialContainer && ((IMaterialContainer) block).isMaterialInBB(world, pos, material)) {
						pos.release();
						return new Hook.Result(Boolean.TRUE);
					}
				}

		pos.release();
		return new Hook.Result();
	}

}
