package index.alchemy.world.biome;

public class BiomeGenTime extends AlchemyBiome {

	public BiomeGenTime() {
		super("time", new PropsBuilder("Time"));
		
//		canSpawnInBiome = false;
//		canGenerateRivers = false;
		canGenerateVillages = false;
		
		spawnableMonsterList.clear();
		spawnableCreatureList.clear();
		spawnableCaveCreatureList.clear();
		spawnableWaterCreatureList.clear();
		
		clearWeights();
	}

}
