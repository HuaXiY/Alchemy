package index.alchemy.world.generator;

import java.util.Random;

import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.api.generation.BOPGeneratorBase;
import biomesoplenty.common.util.block.BlockQuery;
import biomesoplenty.common.util.block.BlockQuery.BlockQueryBlock;
import biomesoplenty.common.util.block.BlockQuery.BlockQueryParseException;
import biomesoplenty.common.util.block.BlockQuery.BlockQueryState;
import biomesoplenty.common.world.generator.GeneratorBigFlower;
import index.alchemy.api.annotation.Generator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;

@Generator(identifier = "big_flower_honey", builder = GeneratorBigFlowerHoney.Builder.class)
public class GeneratorBigFlowerHoney extends GeneratorBigFlower {
	
	public static class Builder extends BOPGeneratorBase.InnerBuilder<Builder, GeneratorBigFlowerHoney>
			implements IGeneratorBuilder<GeneratorBigFlowerHoney> {
		
		protected BigFlowerType flowerType;
		protected IBlockState fluid;
		protected int minLevel, maxLevel;
		protected IBlockPosQuery placeOn;
		protected IBlockPosQuery replace;
		
		public Builder flowerType(BigFlowerType a) { flowerType = a; return self(); }
		public Builder fluid(Fluid a) { fluid = a.getBlock().getDefaultState(); return self(); }
		public Builder fluid(Block a) { fluid = a.getDefaultState(); return self(); }
		public Builder fluid(IBlockState a) { fluid = a; return self(); }
		public Builder minLevel(int a) { minLevel = a; return self(); }
		public Builder maxLevel(int a) { maxLevel = a; return self(); }
		public Builder placeOn(IBlockPosQuery a) { placeOn = a; return self(); }
		public Builder placeOn(String a) throws BlockQueryParseException { placeOn = BlockQuery.parseQueryString(a); return self(); }
		public Builder placeOn(Block a) { placeOn = new BlockQueryBlock(a); return self(); }
		public Builder placeOn(IBlockState a) { placeOn = new BlockQueryState(a); return self(); }
		public Builder replace(IBlockPosQuery a) { replace = a; return self(); }
		public Builder replace(String a) throws BlockQueryParseException { replace = BlockQuery.parseQueryString(a); return self(); }
		public Builder replace(Block a) { replace = new BlockQueryBlock(a); return self(); }
		public Builder replace(IBlockState a) { replace = new BlockQueryState(a); return self(); }
		
		public Builder() {
			 amountPerChunk = 1.0F;
			 flowerType = BigFlowerType.RED;
			 fluid = Blocks.WATER.getDefaultState();
			 minLevel = 0;
			 maxLevel = 15;
			 placeOn = BlockQueries.fertile;
			 replace = BlockQueries.airOrLeaves;
		}
		
		@Override
		public GeneratorBigFlowerHoney create() {
			return new GeneratorBigFlowerHoney(amountPerChunk, flowerType, fluid, minLevel, maxLevel, placeOn, replace);
		}
	
	}
	
	protected IBlockState fluid;
	protected int minLevel, maxLevel;
	
	public GeneratorBigFlowerHoney(float amountPerChunk, BigFlowerType flowerType, IBlockState fluid, int minLevel, int maxLevel,
			IBlockPosQuery placeOn, IBlockPosQuery replace) {
		super(amountPerChunk, placeOn, replace, flowerType);
		this.fluid = fluid;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		boolean result = super.func_180709_b(world, rand, pos);
		if (result)
			switch (flowerType) {
				case RED:
					int level = minLevel + rand.nextInt(maxLevel - minLevel);
					IBlockState state = fluid.withProperty(BlockFluidBase.LEVEL, level);
					world.setBlockState(pos = pos.up(7), state, 2);
					for (EnumFacing facing : EnumFacing.values())
						world.setBlockState(pos.offset(facing), state, 2);
				default:
				break;
			}
		return result;
	}
	
}
