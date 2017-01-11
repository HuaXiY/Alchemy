package index.alchemy.world.generator;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import index.alchemy.api.IGenTerrainBlocks;
import index.alchemy.util.Always;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.IShearable;

public class GenTerrainLakes implements IGenTerrainBlocks {
	
	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	
	public static class Builder extends InnerBuilder<Builder, GenTerrainLakes> implements IGenTerrainBuilder<GenTerrainLakes> {
		
		protected Biome biome;
		protected int minHeight, maxHeight;
		protected IBlockState liquid;
		protected IBlockState frozenLiquid;
		
		public Builder biome(Biome a) { biome = a; return self(); }
		public Builder minHeight(int a) { minHeight = a; return self(); }
		public Builder maxHeight(int a) { maxHeight = a; return self(); }
		public Builder liquid(IBlockState a) { liquid = a; return self(); }
		public Builder liquid(Block a) { liquid = a.getDefaultState(); return self(); }
		public Builder frozenLiquid(IBlockState a) { frozenLiquid = a; return self(); }
		public Builder frozenLiquid(Block a) { frozenLiquid = a.getDefaultState(); return self(); }
		
		public Builder waterLakeForBiome(Biome a) {
			biome = a;
			liquid = Blocks.WATER.getDefaultState();
			frozenLiquid = Blocks.ICE.getDefaultState();
			return this;
		}
		
		public Builder() {
			// defaults
			biome = Biomes.RIVER;
			minHeight = 0;
			maxHeight = Always.maxHeight;
			amountPerChunk = 1.0F;
			liquid = Blocks.WATER.getDefaultState();
			frozenLiquid = Blocks.ICE.getDefaultState();
		}
		
		@Override
		public GenTerrainLakes create() {
			return new GenTerrainLakes(biome, minHeight, maxHeight, amountPerChunk, liquid, frozenLiquid);
		}
	}
	
	protected Biome biome;
	protected int minHeight, maxHeight;
	protected float amountPerChunk;
	protected IBlockState liquid;
	protected IBlockState frozenLiquid;

	public GenTerrainLakes(Biome biome, int minHeight, int maxHeight, float amountPerChunk, IBlockState liquid, IBlockState frozenLiquid) {
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.biome = biome;
		this.amountPerChunk = amountPerChunk;
		this.liquid = liquid;
		this.frozenLiquid = frozenLiquid;
	}
	
	public boolean[] getCavityShape(Random rand) {
		boolean[] cavityShape = new boolean[16 * 16 * 8];
		int numPasses = rand.nextInt(4) + 4;
		int pass;
		
		for (pass = 0; pass < numPasses; pass++) {
			double scaleX = rand.nextDouble() * 6.0D + 3.0D; // between 3 and 9
			double scaleY = rand.nextDouble() * 4.0D + 2.0D; // between 2 and 6
			double scaleZ = rand.nextDouble() * 6.0D + 3.0D; // between 3 and 9
			
			double pointX = rand.nextDouble() * (16.0D - scaleX - 2.0D) + 1.0D + scaleX / 2.0D; // between 2.5 and 13.5
			double pointY = rand.nextDouble() * (8.0D - scaleY - 4.0D) + 2.0D + scaleY / 2.0D; // between 3 and 5
			double pointZ = rand.nextDouble() * (16.0D - scaleZ - 2.0D) + 1.0D + scaleZ / 2.0D; // between 2.5 and 13.5

			for (int x = 1; x < 15; ++x)
				for (int z = 1; z < 15; ++z)
					for (int y = 1; y < 7; ++y) {
						double dx = ((double) x - pointX) / (scaleX / 2.0D);
						double dy = ((double) y - pointY) / (scaleY / 2.0D);
						double dz = ((double) z - pointZ) / (scaleZ / 2.0D);
						
						double r = dx * dx + dy * dy + dz * dz;

						if (r < 1.0D)
							cavityShape[(x * 16 + z) * 8 + y] = true;
					}
		}
		
		return cavityShape;
	}
	
	public boolean isCavityEdge(int x, int y, int z, boolean[] cavityShape) {
		if (cavityShape[(x * 16 + z) * 8 + y])
			return false;
		return x < 15 && cavityShape[((x + 1) * 16 + z) * 8 + y] ||
				x > 0 && cavityShape[((x - 1) * 16 + z) * 8 + y] ||
				z < 15 && cavityShape[(x * 16 + z + 1) * 8 + y] ||
				z > 0 && cavityShape[(x * 16 + (z - 1)) * 8 + y] ||
				y < 7 && cavityShape[(x * 16 + z) * 8 + y + 1] ||
				y > 0 && cavityShape[(x * 16 + z) * 8 + (y - 1)];
	}
	
	public boolean isCavityViable(ChunkPrimer chunkPrimer, BlockPos startPos, boolean[] cavityShape) {		
		for (int x = 0; x < 16; ++x)
			for (int z = 0; z < 16; ++z)
				for (int y = 0; y < 8; ++y)
					if (isCavityEdge(x, y, z, cavityShape)) {

						BlockPos pos = startPos.add(x, y, z);
						Material material = chunkPrimer.getBlockState(pos.getX(), pos.getY(), pos.getZ()).getMaterial();

						// abandon if there's liquid at the edge of the cavity above the water level
						if (y >= 4 && material.isLiquid())
							return false;

						// abandon if there's a different liquid at the edge of the cavity below the water level
						if (y < 4 && !material.isSolid() &&
								chunkPrimer.getBlockState(pos.getX(), pos.getY(), pos.getZ()).getBlock() != liquid)
							return false;
					}
		return true;
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int cx, int cz, double noiseVal) {
		
		if (rand.nextFloat() > amountPerChunk)
			return;
		
		int cy = 256;
		// move to start of square
		cx -= 8;
		cz -= 8;
		
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		
		// move downwards to the first non-air block
		while (cy > 5 && chunkPrimer.getBlockState(cx, cy, cz).getBlock() == Blocks.AIR)
			cy--;
		
		// abandon if there isn't 4 blocks beneath pos
		if (cy < minHeight || cy > maxHeight || chunkPrimer.getBlockState(cx, cy, cz) != biome.topBlock)
			return;

		// move down 4 blocks (to bottom of lake-to-be)
		cy -= 4;

		// create a random cavity
		boolean[] cavityShape = getCavityShape(rand);
		
		// check it's viable
		if (!isCavityViable(chunkPrimer, pos.setPos(cx, cy, cz), cavityShape))
			return;
		
		List<BlockPos> list = Lists.newLinkedList();
		
		for (int x = 0; x < 16; ++x)
			for (int z = 0; z < 16; ++z)
				for (int i = 3; i < 6; i++)
					if (cavityShape[(x * 16 + z) * 8 + i]) {
						int y = i + 1;
						while (true) {
							IBlockState state = chunkPrimer.getBlockState(cx + x, cy + y, cz + z);
							if (state == AIR)
								break;
							if (state.getBlock() instanceof IShearable)
								list.add(new BlockPos(cx + x, cy + y, cz + z));
							else if (state.getMaterial() != Material.WOOD)
								break;
							else
								return;
							y++;
						}
					}
				
		
		list.forEach(p -> chunkPrimer.setBlockState(p.getX(), p.getY(), p.getZ(), AIR));
		
		int x, y, z;

		// create the lake - bottom 4 layers of turned to water, top 4 turned to air
		for (x = 0; x < 16; x++)
			for (z = 0; z < 16; z++)
				for (y = 0; y < 6; y++)
					if (cavityShape[(x * 16 + z) * 8 + y])
						chunkPrimer.setBlockState(cx + x, cy + y, cz + z, y > 3 ? AIR :
							world.canBlockFreezeWater(pos.setPos(cx + x, cy + y, cz + z)) ? frozenLiquid : liquid);
		
	}

}