package index.alchemy.world.biome;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import biomesoplenty.common.biome.overworld.BOPBiome;
import index.alchemy.api.IGenTerrainBlocks;
import index.alchemy.api.IRegister;
import index.alchemy.util.FakeChunkPrimer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class AlchemyBiome extends BOPBiome implements IRegister {
	
	protected final Map<String, IGenTerrainBlocks> terrainGenerators = Maps.newHashMap();
	
	protected final ThreadLocal<Boolean> resting = ThreadLocal.withInitial(Boolean.TRUE::booleanValue);
	
	public Map<String, IGenTerrainBlocks> getTerrainGenerators() {
		return terrainGenerators;
	}
	
	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int x, int z, double noiseVal) {
		super.genTerrainBlocks(world, rand, chunkPrimer, x, z, noiseVal);
		if (resting.get()) {
			resting.set(false);
			terrainGenerators.values().forEach(g -> g.genTerrainBlocks(world, rand,
					new FakeChunkPrimer(world, chunkPrimer, x, z), x, z, noiseVal));
			resting.set(true);
		}
	}
	
	public AlchemyBiome(String name, PropsBuilder defaultBuilder) {
		super(name, defaultBuilder);
		setRegistryName(name);
		register();
	}
	
}
