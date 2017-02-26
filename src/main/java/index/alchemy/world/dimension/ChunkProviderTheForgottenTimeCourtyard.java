package index.alchemy.world.dimension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import index.alchemy.world.biome.AlchemyBiomeLoader;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

public class ChunkProviderTheForgottenTimeCourtyard implements IChunkGenerator {
	
	protected final World world;
	protected final Random random;
	protected final List<SpawnListEntry> spawns = Collections.emptyList();
	
	public ChunkProviderTheForgottenTimeCourtyard(World world, long seed) {
		this.world = world;
		this.random = new Random(seed);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		random.setSeed(x * 341873128712L + z * 132897987541L);
		ChunkPrimer primer = new ChunkPrimer();
		
		Chunk chunk = new Chunk(world, primer, x, z);
		byte biomeId = (byte) Biome.getIdForBiome(AlchemyBiomeLoader.time);
		Arrays.fill(chunk.getBiomeArray(), biomeId);
		chunk.resetRelightChecks();
		return chunk;
	}

	@Override
	public void populate(int x, int z) { }

	@Override
	public boolean generateStructures(Chunk chunk, int x, int z) { return false; }

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) { return spawns; }

	@Override
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) { return null; }

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {
		// TODO Auto-generated method stub
		
	}

}
