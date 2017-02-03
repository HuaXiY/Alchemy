package index.alchemy.world.biome;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import biomesoplenty.common.biome.overworld.BOPBiome;
import index.alchemy.api.IAlchemyBiome;
import index.alchemy.api.IGenTerrainBlocks;
import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Listener;
import index.alchemy.util.FakeChunkPrimer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

@Listener(Listener.Type.TERRAIN)
public class AlchemyBiome extends BOPBiome implements IAlchemyBiome, IRegister {
	
	protected final Map<String, IGenTerrainBlocks> terrainGenerators = Maps.newHashMap();
	
	protected static final ThreadLocal<Boolean> resting = ThreadLocal.withInitial(Boolean.TRUE::booleanValue);
	
	public Map<String, IGenTerrainBlocks> getTerrainGenerators() {
		return terrainGenerators;
	}
	
	@Override
	public boolean canGenerateVillages() {
		return canGenerateVillages;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPopulateChunk_Populate(PopulateChunkEvent.Populate event) {
		Biome biome = event.getWorld().getBiome(new BlockPos(event.getChunkX() * 16, 1, event.getChunkZ() * 16));
		if (biome instanceof AlchemyBiome) {
			if (event.getType() == PopulateChunkEvent.Populate.EventType.LAKE ||
				event.getType() == PopulateChunkEvent.Populate.EventType.LAVA)
				event.setResult(Result.DENY);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDecorateBiome_Decorate(DecorateBiomeEvent.Decorate event) {
		if (event.getWorld().getBiome(event.getPos()) instanceof AlchemyBiome)
			event.setResult(Result.DENY);
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
