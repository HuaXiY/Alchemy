package index.alchemy.world.generator;

import java.util.Random;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.config.IConfigObj;
import biomesoplenty.api.generation.BOPGeneratorBase;
import index.alchemy.api.annotation.Generator;
import index.alchemy.util.Always;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

@Generator(identifier = "lakes_pass", builder = GeneratorLakesPass.Builder.class)
public class GeneratorLakesPass extends BOPGeneratorBase {
	
	protected static final IBlockState
			AIR = Blocks.AIR.getDefaultState(),
			MUD = BOPBlocks.mud.getDefaultState(),
			WATER = Blocks.WATER.getDefaultState();
	
	public static class Builder extends BOPGeneratorBase.InnerBuilder<Builder, GeneratorLakesPass>
			implements IGeneratorBuilder<GeneratorLakesPass> {
		
		protected int minHeight, maxHeight;
		protected Biome biome;
		
		public Builder minHeight(int a) { minHeight = a; return self(); }
		public Builder maxHeight(int a) { maxHeight = a; return self(); }
		public Builder biome(Biome a) { biome = a; return self(); }
		
		public Builder() {
			// defaults
			minHeight = 0;
			maxHeight = Always.maxHeight;
			biome = Biomes.RIVER;
		}

		@Override
		public GeneratorLakesPass create() {
			return new GeneratorLakesPass(amountPerChunk, minHeight, maxHeight, biome);
		}
		
	}
	
	protected int minHeight, maxHeight;
	protected Biome biome;

	protected GeneratorLakesPass(float amountPerChunk, int minHeight, int maxHeight, Biome biome) {
		super(amountPerChunk);
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.biome = biome;
	}
	
	@Override
	public void scatter(World world, Random rand, BlockPos pos) {
		generate(world, rand, pos);
	}

	@Override
	public void configure(IConfigObj conf) { }

	@Override
	public BlockPos getScatterY(World world, Random random, int x, int z) {
		return BlockPos.ORIGIN;
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		for (int x = -8; x < 24; x++)
			for (int z = -8; z < 24; z++) {
				if (world.getBiome(pos.add(x, 0, z)) != biome)
					continue;
				boolean water = false, mud = false;
				IBlockState prev = AIR;
				for (int y = maxHeight; y > minHeight; y--) {
					BlockPos now = pos.add(x, y, z);
					IBlockState state = world.getBlockState(now);
					if (mud) {
						world.setBlockState(now, MUD, 2);
						break;
					}
					if (state == AIR)
						continue;
					if (state == WATER)
						water = true;
					else if (water) {
						world.setBlockState(now, MUD, 2);
						mud = true;
					} else if (state == biome.fillerBlock) {
						if (prev == AIR || prev.getMaterial() != Material.GRASS && prev.getMaterial() != Material.GROUND)
							world.setBlockState(now, biome.topBlock, 2);
					} else if (state.getMaterial() == Material.ROCK && y < minHeight)
						break;
					prev = state;
				}
			}
		return true;
	}

}
