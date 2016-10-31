package index.alchemy.interacting;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import index.alchemy.api.annotation.Init;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.POSTINITIALIZED)
public class Elemix {
	
	public static boolean blockCanToIce(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}
	
	public static final List<Predicate<IBlockState>> heat_source_list = new LinkedList<>();
	static {
		heat_source_list.add(s -> s.getBlock() == Blocks.FIRE);
		heat_source_list.add(s -> s == ModBlocks.bop$flower_burning_blossom);
	}
	
	public static boolean blockIsHeatSource(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		for (Predicate<IBlockState> predicate : heat_source_list)
			if (predicate.test(state))
				return true;
		return false;
	}
	
}
