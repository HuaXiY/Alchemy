package index.alchemy.world.biome;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.enums.BOPFlowers;
import biomesoplenty.api.enums.BOPGems;
import biomesoplenty.api.enums.BOPPlants;
import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.api.enums.BOPWoods;
import biomesoplenty.api.generation.GeneratorStage;
import biomesoplenty.common.block.BlockBOPBamboo;
import biomesoplenty.common.block.BlockBOPDoublePlant;
import biomesoplenty.common.block.BlockBOPGrass;
import biomesoplenty.common.block.BlockBOPLeaves;
import biomesoplenty.common.block.BlockBOPLilypad;
import biomesoplenty.common.block.BlockBOPMushroom;
import biomesoplenty.common.entities.EntityButterfly;
import biomesoplenty.common.entities.EntitySnail;
import biomesoplenty.common.fluids.HoneyFluid;
import biomesoplenty.common.util.block.BlockQuery;
import biomesoplenty.common.entities.EntityPixie;
import biomesoplenty.common.world.generator.GeneratorBigFlower;
import biomesoplenty.common.world.generator.GeneratorBigMushroom;
import biomesoplenty.common.world.generator.GeneratorColumns;
import biomesoplenty.common.world.generator.GeneratorDoubleFlora;
import biomesoplenty.common.world.generator.GeneratorFlora;
import biomesoplenty.common.world.generator.GeneratorGrass;
import biomesoplenty.common.world.generator.GeneratorOreSingle;
import biomesoplenty.common.world.generator.GeneratorWeighted;
import biomesoplenty.common.world.generator.tree.GeneratorBulbTree;
import biomesoplenty.common.world.generator.tree.GeneratorBush;
import biomesoplenty.common.world.generator.tree.GeneratorPalmTree;
import index.alchemy.api.annotation.Hook;
import index.alchemy.world.generator.BlockQueries;
import index.alchemy.world.generator.GenTerrainLakes;
import index.alchemy.world.generator.GeneratorBigFlowerHoney;
import index.alchemy.world.generator.GeneratorFruitTree;
import index.alchemy.world.generator.GeneratorLakesPass;
import index.project.version.annotation.Alpha;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

@Alpha
@Hook.Provider
public class BiomeGenDragonIsland extends AlchemyBiome {
	
	@Hook("biomesoplenty.common.block.BlockBOPBamboo#canBlockStay")
	public static final Hook.Result canBlockStay(BlockBOPBamboo bamboo, World world, BlockPos pos, IBlockState state) {
		IBlockState down = world.getBlockState(pos.down());
		if (down.getBlock() instanceof BlockBOPGrass && down.getValue(BlockBOPGrass.VARIANT) == BlockBOPGrass.BOPGrassType.DAISY)
			return Hook.Result.TRUE;
		return Hook.Result.VOID;
	}
	
	public BiomeGenDragonIsland() {
		super("dragon_island", new PropsBuilder("Dragon Island").withTemperature(1.1F).withRainfall(1.0F).withGuiColour(2211330));

		terrainSettings.avgHeight(72).heightVariation(-2, 60).octaves(0, 1, 2, 2, 1, 0).sidewaysNoise(0.2);
		
		topBlock = BOPBlocks.grass.getDefaultState().withProperty(BlockBOPGrass.VARIANT, BlockBOPGrass.BOPGrassType.DAISY);
		fillerBlock = Blocks.DIRT.getStateFromMeta(BlockDirt.DirtType.DIRT.getMetadata());
		//addWeight(BOPClimates.HOT_DESERT, 10);
		
		canSpawnInBiome = true;
		canGenerateVillages = false;
		canGenerateRivers = false;
		
		skyColor = 0x73D9FF;
		
		spawnableCreatureList.clear();
		spawnableMonsterList.clear();
		
		spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 8, 2, 4));
		spawnableCreatureList.add(new SpawnListEntry(EntityButterfly.class, 16, 3, 6));
		spawnableCreatureList.add(new SpawnListEntry(EntitySnail.class, 8, 2, 3));
		spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 4, 4, 6));
		spawnableCreatureList.add(new SpawnListEntry(EntityPixie.class, 1, 1, 2));
		
		//spawnableMonsterList.add(new SpawnListEntry(EntityBat.class, 100, 8, 8));
		
		clearWeights();
		
		// lake
		terrainGenerators.put("lake", new GenTerrainLakes.Builder()
				.minHeight(50)
				.maxHeight(80)
				.waterLakeForBiome(this)
				.amountPerChunk(0.001F)
				.create()
		);
		
		// lake pass
		addGenerator("lake_pass", GeneratorStage.SAND, new GeneratorLakesPass.Builder()
			.minHeight(45)
			.maxHeight(80)
			.biome(this)
			.create()
		);
		
		// trees
		GeneratorWeighted treeGenerator = new GeneratorWeighted(16F);
		addGenerator("trees", GeneratorStage.TREE, treeGenerator);
		treeGenerator.add("palm", 12, new GeneratorPalmTree.Builder()
				.log(BOPWoods.PALM)
				.leaves(BlockBOPLeaves.paging.getVariantState(BOPTrees.PALM).withProperty(BlockOldLeaf.CHECK_DECAY, false))
				.create()
		);
		treeGenerator.add("jungle_twiglet", 8, new GeneratorFruitTree.Builder()
				.minHeight(2)
				.maxHeight(2)
				.log(BlockPlanks.EnumType.JUNGLE)
				.leaves(BlockPlanks.EnumType.JUNGLE)
				.trunkFruit(Blocks.COCOA.getDefaultState())
				.maxAge(3)
				.fruitAgeHandler((state, age) -> state.withProperty(BlockCocoa.AGE, age))
				.replace(BlockQueries.air)
				.create()
		);
		treeGenerator.add("bamboo", 6, new GeneratorBulbTree.Builder()
				.minHeight(10)
				.maxHeight(20)
				.log(BOPBlocks.bamboo.getDefaultState())
				.leaves(BOPTrees.BAMBOO)
				.create()
		);
		treeGenerator.add("red_big_flowers", 1, new GeneratorBigFlowerHoney.Builder()
				.flowerType(GeneratorBigFlower.BigFlowerType.RED)
				.fluid(HoneyFluid.instance)
				.minLevel(0)
				.maxLevel(5)
				.create()
		);
		treeGenerator.add("oak_bush", 4, new GeneratorBush.Builder()
				.maxHeight(2)
				.altLeaves(BOPTrees.FLOWERING)
				.replace(BlockQueries.air)
				.create()
		);
		
		// big mushroom
		addGenerator("big_red_mushroom", GeneratorStage.BIG_SHROOM, new GeneratorBigMushroom.Builder()
				.amountPerChunk(0.4F)
				.mushroomType(GeneratorBigMushroom.BigMushroomType.RED)
				.create()
		);
		
		// grasses
		GeneratorWeighted grassGenerator = new GeneratorWeighted(6F);
		addGenerator("grass", GeneratorStage.GRASS, grassGenerator);
		grassGenerator.add("flax", 1, new GeneratorDoubleFlora.Builder()
				.with(BlockBOPDoublePlant.DoublePlantType.FLAX)
				.placeOn(topBlock)
				.create()
		);
		grassGenerator.add("tallgrass", 1, new GeneratorDoubleFlora.Builder()
				.with(BlockDoublePlant.EnumPlantType.GRASS)
				.placeOn(topBlock)
				.create()
		);
		grassGenerator.add("shortgrass", 1, new GeneratorGrass.Builder()
				.with(BOPPlants.SHORTGRASS)
				.placeOn(topBlock)
				.create()
		);
		grassGenerator.add("mediumgrass", 1, new GeneratorGrass.Builder()
				.with(BOPPlants.MEDIUMGRASS)
				.placeOn(topBlock)
				.create()
		);
		grassGenerator.add("dampgrass", 1, new GeneratorGrass.Builder()
				.with(BOPPlants.DAMPGRASS)
				.placeOn(topBlock)
				.create()
		);
		
		// green
		GeneratorWeighted greenGenerator = new GeneratorWeighted(8F);
		addGenerator("green", GeneratorStage.GRASS, greenGenerator);
		greenGenerator.add("leaf_piles", 8, new GeneratorFlora.Builder()
				.with(BOPPlants.LEAFPILE)
				.placeOn(BlockQuery.buildAnd()
							.states(topBlock)
							.add(BlockQueries.leavesSide)
							.create())
				.create()
		);
		greenGenerator.add("sprouts", 1, new GeneratorFlora.Builder()
				.with(BOPPlants.SPROUT)
				.placeOn(topBlock)
				.create()
		);
		greenGenerator.add("shrubs", 1, new GeneratorFlora.Builder()
				.with(BOPPlants.SHRUB)
				.placeOn(topBlock)
				.create()
		);
		
		// pumpkin
		GeneratorWeighted pumpkinGenerator = new GeneratorWeighted(0.5F);
		addGenerator("pumpkin", GeneratorStage.PUMPKIN, pumpkinGenerator);
		pumpkinGenerator.add("melons", 4, new GeneratorFlora.Builder()
				.with(Blocks.MELON_BLOCK.getDefaultState())
				.placeOn(topBlock)
				.create()
		);
		pumpkinGenerator.add("pumpkin", 1, new GeneratorFlora.Builder()
				.with(Blocks.PUMPKIN.getDefaultState())
				.placeOn(topBlock)
				.create()
		);
		
		// flowers
		GeneratorWeighted flowerGenerator = new GeneratorWeighted(4F);
		addGenerator("flowers", GeneratorStage.FLOWERS, flowerGenerator);
		flowerGenerator.add("hibiscus", 3, new GeneratorFlora.Builder()
				.with(BOPFlowers.PINK_DAFFODIL)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("blue_hydrangeas", 3, new GeneratorFlora.Builder()
				.with(BOPFlowers.BLUE_HYDRANGEA)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("glow_flower", 1, new GeneratorFlora.Builder()
				.with(BOPFlowers.GLOWFLOWER)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("wild_flower", 1, new GeneratorFlora.Builder()
				.with(BOPFlowers.WILDFLOWER)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("berry_bush", 1, new GeneratorFlora.Builder()
				.with(BOPPlants.BERRYBUSH)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("allium", 1, new GeneratorFlora.Builder()
				.with(BlockFlower.EnumFlowerType.ALLIUM)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("rafflesia", 1, new GeneratorFlora.Builder()
				.with(BOPPlants.RAFFLESIA)
				.placeOn(topBlock)
				.create()
		);
		flowerGenerator.add("syringa", 1, new GeneratorDoubleFlora.Builder()
				.with(BlockDoublePlant.EnumPlantType.SYRINGA)
				.placeOn(topBlock)
				.create()
		);
		
		// waterside
		GeneratorWeighted watersideGenerator = new GeneratorWeighted(8F);
		addGenerator("waterside", GeneratorStage.FLOWERS, watersideGenerator);
		watersideGenerator.add("cattail", 6, new GeneratorFlora.Builder()
				.with(BOPPlants.CATTAIL)
				.placeOn(BlockQueries.litFertileWaterside)
				.create()
		);
		watersideGenerator.add("tail_cattail", 4, new GeneratorDoubleFlora.Builder()
				.with(BlockBOPDoublePlant.DoublePlantType.TALL_CATTAIL)
				.placeOn(BlockQueries.litFertileWaterside)
				.create()
		);
		watersideGenerator.add("reeds", 6, new GeneratorColumns.Builder()
				.generationAttempts(24)
				.placeOn(BlockQueries.litFertileWaterside)
				.with(Blocks.REEDS.getDefaultState())
				.minHeight(1)
				.maxHeight(3)
				.create()
		);
		
		// water above
		GeneratorWeighted waterAboveGenerator = new GeneratorWeighted(6F);
		addGenerator("water_above", GeneratorStage.LILYPAD, waterAboveGenerator);
		waterAboveGenerator.add("reed", 4, new GeneratorFlora.Builder()
				.with(BOPPlants.REED)
				.create()
		);
		waterAboveGenerator.add("waterlily", 2, new GeneratorFlora.Builder()
				.with(Blocks.WATERLILY.getDefaultState())
				.create()
		);
		waterAboveGenerator.add("small", 1, new GeneratorFlora.Builder()
				.with(BlockBOPLilypad.LilypadType.SMALL)
				.create()
		);
		waterAboveGenerator.add("tiny", 1, new GeneratorFlora.Builder()
				.with(BlockBOPLilypad.LilypadType.TINY)
				.create()
		);
		waterAboveGenerator.add("medium", 2, new GeneratorFlora.Builder()
				.with(BlockBOPLilypad.LilypadType.MEDIUM)
				.create()
		);
		waterAboveGenerator.add("flower", 3, new GeneratorFlora.Builder()
				.with(BlockBOPLilypad.LilypadType.FLOWER)
				.create()
		);
		
		// shroom
		GeneratorWeighted shroomGenerator = new GeneratorWeighted(1F);
		addGenerator("shroom", GeneratorStage.SHROOM, flowerGenerator);
		flowerGenerator.add("glow_shroom", 1, new GeneratorFlora.Builder()
				.with(BlockBOPMushroom.MushroomType.GLOWSHROOM)
				.create()
		);

		// gem
		addGenerator("ruby", GeneratorStage.SAND, new GeneratorOreSingle.Builder()
				.amountPerChunk(24)
				.with(BOPGems.RUBY)
				.create()
		);
		addGenerator("sapphire", GeneratorStage.SAND, new GeneratorOreSingle.Builder()
				.amountPerChunk(12)
				.with(BOPGems.SAPPHIRE)
				.create()
		);
		
		BiomeDictionary.registerBiomeType(this, Type.HOT, Type.DENSE, Type.WET, Type.MAGICAL, Type.FOREST, Type.MOUNTAIN);
	}
	
}
