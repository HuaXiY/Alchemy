package index.alchemy.world.generator;

import java.util.Random;
import java.util.function.BiFunction;

import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.config.IConfigObj;
import biomesoplenty.common.util.biome.GeneratorUtils.ScatterYMethod;
import biomesoplenty.common.world.generator.tree.GeneratorBasicTree;
import index.alchemy.api.annotation.Generator;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Generator(identifier = "fruit_tree", builder = GeneratorFruitTree.Builder.class)
public class GeneratorFruitTree extends GeneratorBasicTree {
	
	public static class Builder extends GeneratorBasicTree.InnerBuilder<Builder, GeneratorFruitTree>
			implements IGeneratorBuilder<GeneratorFruitTree> {
		
		protected int minAge, maxAge;
		protected BiFunction<IBlockState, Integer, IBlockState> fruitAgeHandler;
		
		public Builder minAge(int a) { minAge = a; return self(); }
		public Builder maxAge(int a) { maxAge = a; return self(); }
		public Builder fruitAgeHandler(BiFunction<IBlockState, Integer, IBlockState> a) { fruitAgeHandler = a; return self(); }
		
		public Builder() {
			minAge = 0;
			maxAge = 0;
			fruitAgeHandler = (state, age) -> state;
			amountPerChunk = 1.0F;
			minHeight = 2;
			maxHeight = 6;
			placeOn = BlockQueries.fertile;
			replace = BlockQueries.airOrLeaves;
			log = Blocks.LOG.getDefaultState();
			leaves = Blocks.LEAVES.getDefaultState();
			vine = null;
			hanging = null;
			trunkFruit = null;
			altLeaves = null;
			scatterYMethod = ScatterYMethod.AT_SURFACE;
		}

		@Override
		public GeneratorFruitTree create() {
			return new GeneratorFruitTree(amountPerChunk, placeOn, replace, log, leaves, vine, hanging,
					trunkFruit, altLeaves, minHeight, maxHeight, minAge, maxAge, fruitAgeHandler,
					updateNeighbours, leafLayers, leavesOffset, maxLeavesRadius, leavesLayerHeight,
					placeVinesOn, hangingChance, scatterYMethod);
		}
	}
	
	protected int minAge, maxAge;
	protected BiFunction<IBlockState, Integer, IBlockState> fruitAgeHandler;
	
	public GeneratorFruitTree(float amountPerChunk, IBlockPosQuery placeOn, IBlockPosQuery replace, IBlockState log, IBlockState leaves,
			IBlockState vine, IBlockState hanging, IBlockState trunkFruit, IBlockState altLeaves, int minHeight, int maxHeight,
			int minAge, int maxAge, BiFunction<IBlockState, Integer, IBlockState> fruitAgeHandler, boolean updateNeighbours, int leafLayers, int leavesOffset,
			int maxLeavesRadius, int leavesLayerHeight, IBlockPosQuery placeVinesOn, float hangingChance, ScatterYMethod scatterYMethod) {
		super(amountPerChunk, placeOn, replace, log, leaves, vine, hanging, trunkFruit, altLeaves, minHeight, maxHeight, updateNeighbours,
				leafLayers, leavesOffset, maxLeavesRadius, leavesLayerHeight, placeVinesOn, hangingChance, scatterYMethod);
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.fruitAgeHandler = fruitAgeHandler;
	}

	@Override
	public boolean generate(World world, Random random, BlockPos startPos) {

		// Move down until we reach the ground
		while (startPos.getY() > 1 && world.isAirBlock(startPos) ||
				world.getBlockState(startPos).getBlock().isLeaves(world.getBlockState(startPos), world, startPos))
			startPos = startPos.down();
		
		if (!placeOn.matches(world, startPos))
			return false;
		
		// choose a random height
		int height = minHeight + random.nextInt(1 + maxHeight - minHeight);
		int baseHeight = height / 3;
		
		// start from the block above the ground block
		BlockPos pos = startPos.up();
		
		// add log and leaves on each level
		for (int y = 0; y < height; y++) {
			if (!setLog(world, pos.up(y)))
				return true;
			if (y <= baseHeight)
				continue;
			setLeaves(world, pos.add(1, y, 0));
			setLeaves(world, pos.add(-1, y, 0));
			setLeaves(world, pos.add(0, y, 1));
			setLeaves(world, pos.add(0, y, -1));
			
			if (trunkFruit != null)
				if (random.nextInt(3) == 0)
					for (int i = 0; i < 2; ++i)
						for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
							if (random.nextInt(4 - i) == 0) {
								EnumFacing opposite = facing.getOpposite();
								generateTrunkFruit(world, minAge + random.nextInt(maxAge - minAge), pos.add(opposite.getFrontOffsetX(),
										0, opposite.getFrontOffsetZ()), opposite);
							}
		}
		// finish with leaves on top
		setLeaves(world, pos.add(0, height, 0));
		return true;
	}
	
	protected void generateTrunkFruit(World world, int age, BlockPos pos, EnumFacing direction) {
		IBlockState newFruit = fruitAgeHandler.apply(trunkFruit, age);
		if (newFruit != null) {
			if (trunkFruit.getPropertyKeys().contains(BlockHorizontal.FACING))
				trunkFruit = trunkFruit.withProperty(BlockCocoa.FACING, direction.getOpposite());
			setTrunkFruit(world, pos);
		}
	}
	
	@Override
	public void configure(IConfigObj conf) {
		amountPerChunk = conf.getFloat("amountPerChunk", amountPerChunk);
		minHeight = conf.getInt("minHeight", minHeight);
		maxHeight = conf.getInt("maxHeight", maxHeight);
		log = conf.getBlockState("logState", log);
		leaves = conf.getBlockState("leavesState", leaves);
	}

}
