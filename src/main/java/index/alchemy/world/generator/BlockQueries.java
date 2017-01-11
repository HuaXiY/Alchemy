package index.alchemy.world.generator;

import biomesoplenty.api.block.IBlockPosQuery;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;

public class BlockQueries extends biomesoplenty.api.block.BlockQueries {
	
	public static IBlockPosQuery leavesSide = (world, pos) -> {
		pos = pos.up();
		for (EnumFacing facing : EnumFacing.HORIZONTALS)
			if (world.getBlockState(pos.offset(facing)).getMaterial() == Material.LEAVES)
				return true;
		return false;
	};

}
