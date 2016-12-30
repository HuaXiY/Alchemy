package index.alchemy.api;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface IMaterialContainer {
	
	boolean isMaterialInBB(World world, BlockPos pos, Material material);

}
