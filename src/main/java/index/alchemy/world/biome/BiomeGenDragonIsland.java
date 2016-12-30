package index.alchemy.world.biome;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.enums.BOPFlowers;
import biomesoplenty.api.enums.BOPGems;
import biomesoplenty.api.enums.BOPPlants;
import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.api.enums.BOPWoods;
import biomesoplenty.api.generation.GeneratorStage;
import biomesoplenty.common.block.BlockBOPBamboo;
import biomesoplenty.common.block.BlockBOPGrass;
import biomesoplenty.common.block.BlockBOPLeaves;
import biomesoplenty.common.block.BlockBOPMushroom;
import biomesoplenty.common.entities.EntityButterfly;
import biomesoplenty.common.entities.EntitySnail;
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
import biomesoplenty.common.world.generator.tree.GeneratorTwigletTree;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.Hook;
import index.alchemy.world.generator.GenTerrainLakes;
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
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

@Alpha
@Hook.Provider
public class BiomeGenDragonIsland extends AlchemyBiome implements IEventHandle.Terrain {
	
	@Hook("biomesoplenty.common.block.BlockBOPBamboo#canBlockStay")
	public static final Hook.Result canBlockStay(BlockBOPBamboo bamboo, World world, BlockPos pos, IBlockState state) {
		IBlockState down = world.getBlockState(pos.down());
		if (down.getBlock() instanceof BlockBOPGrass && down.getValue(BlockBOPGrass.VARIANT) == BlockBOPGrass.BOPGrassType.DAISY)
			return Hook.Result.TRUE;
		return Hook.Result.VOID;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPopulateChunk_Populate(PopulateChunkEvent.Populate event) {
		if (event.getWorld().getBiome(new BlockPos(event.getChunkX() * 16, 1, event.getChunkZ() * 16)) == this)
			if (event.getType() == PopulateChunkEvent.Populate.EventType.LAKE ||
				event.getType() == PopulateChunkEvent.Populate.EventType.LAVA)
				event.setResult(Result.DENY);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDecorateBiome_Decorate(DecorateBiomeEvent.Decorate event) {
		if (event.getWorld().getBiome(event.getPos()) == this)
			event.setResult(Result.DENY);
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
		
		spawnableMonsterList.add(new SpawnListEntry(EntityBat.class, 100, 8, 8));
		
		clearWeights();
		
		terrainGenerators.put("lake", new GenTerrainLakes.Builder()
				.minHeight(50)
				.maxHeight(80)
				.waterLakeForBiome(this)
				.amountPerChunk(0.005F)
				.create()
		);
		
		// lake
		addGenerator("lake_pass", GeneratorStage.SAND, new GeneratorLakesPass.Builder()
			.minHeight(45)
			.maxHeight(80)
			.biome(this)
			.create()
		);
		
		// trees
		GeneratorWeighted treeGenerator = new GeneratorWeighted(18F);
		addGenerator("trees", GeneratorStage.TREE, treeGenerator);
		treeGenerator.add("palm", 16, new GeneratorPalmTree.Builder()
				.log(BOPWoods.PALM)
				.leaves(BlockBOPLeaves.paging.getVariantState(BOPTrees.PALM).withProperty(BlockOldLeaf.CHECK_DECAY, false))
				.placeOn(topBlock)
				.create()
		);
		treeGenerator.add("jungle_twiglet", 20, new GeneratorFruitTree.Builder()
				.minHeight(2)
				.maxHeight(2)
				.log(BlockPlanks.EnumType.JUNGLE)
				.leaves(BlockPlanks.EnumType.JUNGLE)
				.trunkFruit(Blocks.COCOA.getDefaultState())
				.fruitAgeHandler((state, age) -> state.withProperty(BlockCocoa.AGE, age))
				.replace(Blocks.AIR)
				.placeOn(topBlock)
				.create()
		);
		treeGenerator.add("bamboo", 5, new GeneratorBulbTree.Builder()
				.minHeight(10)
				.maxHeight(20)
				.log(BOPBlocks.bamboo.getDefaultState())
				.leaves(BOPTrees.BAMBOO)
				.placeOn(topBlock)
				.create()
		);
        treeGenerator.add("bamboo_thin", 5, new GeneratorTwigletTree.Builder()
        		.minHeight(5)
        		.maxHeight(10)
        		.leafChance(0.3F)
        		.log(BOPBlocks.bamboo.getDefaultState())
        		.leaves(BOPTrees.BAMBOO)
        		.placeOn(topBlock)
        		.create()
        );
        treeGenerator.add("red_big_flowers", 1, new GeneratorBigFlower.Builder()
        		.flowerType(GeneratorBigFlower.BigFlowerType.RED)
        		.placeOn(topBlock)
        		.create()
        );
        treeGenerator.add("oak_bush", 4, new GeneratorBush.Builder()
        		.maxHeight(2)
        		.altLeaves(BOPTrees.FLOWERING)
        		.placeOn(topBlock)
        		.create()
        );
        
        // big mushroom
        addGenerator("big_red_mushroom", GeneratorStage.BIG_SHROOM, new GeneratorBigMushroom.Builder()
        		.amountPerChunk(0.4F)
        		.mushroomType(GeneratorBigMushroom.BigMushroomType.RED)
        		.placeOn(topBlock)
        		.create()
        );
		
		// grasses
		GeneratorWeighted grassGenerator = new GeneratorWeighted(6F);
		addGenerator("grass", GeneratorStage.GRASS, grassGenerator);
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
		addGenerator("tallgrass", GeneratorStage.GRASS, new GeneratorDoubleFlora.Builder()
				.with(BlockDoublePlant.EnumPlantType.GRASS)
				.amountPerChunk(0.3F)
				.placeOn(topBlock)
				.create()
		);
		
		//other plants
		addGenerator("leaf_piles", GeneratorStage.FLOWERS, new GeneratorFlora.Builder()
				.amountPerChunk(2F).with(BOPPlants.LEAFPILE)
				.create()
		);
		addGenerator("sprouts", GeneratorStage.FLOWERS, new GeneratorFlora.Builder()
				.amountPerChunk(0.2F).with(BOPPlants.SPROUT)
				.create()
		);
		addGenerator("shrubs", GeneratorStage.FLOWERS, new GeneratorFlora.Builder()
				.amountPerChunk(0.2F).with(BOPPlants.SHRUB)
				.create()
		);
		addGenerator("melons", GeneratorStage.PUMPKIN, new GeneratorFlora.Builder()
				.amountPerChunk(0.3F)
				.with(Blocks.MELON_BLOCK.getDefaultState())
				.placeOn(topBlock)
				.create()
		);
		
		// flowers
		GeneratorWeighted flowerGenerator = new GeneratorWeighted(6F);
		addGenerator("flowers", GeneratorStage.FLOWERS, flowerGenerator);
		flowerGenerator.add("hibiscus", 3, new GeneratorFlora.Builder()
				.with(BOPFlowers.PINK_DAFFODIL)
				.create()
		);
		flowerGenerator.add("blue_hydrangeas", 3, new GeneratorFlora.Builder()
				.with(BOPFlowers.BLUE_HYDRANGEA)
				.create()
		);
		flowerGenerator.add("berry_bush", 1, new GeneratorFlora.Builder()
				.with(BOPPlants.BERRYBUSH)
				.create()
		);
		flowerGenerator.add("glow_flower", 2, new GeneratorFlora.Builder()
				.with(BOPFlowers.GLOWFLOWER)
				.create()
		);
		flowerGenerator.add("allium", 1, new GeneratorFlora.Builder()
				.with(BlockFlower.EnumFlowerType.ALLIUM)
				.create()
		);
		
		// waterside
		GeneratorWeighted waterside = new GeneratorWeighted(8F);
		addGenerator("waterside", GeneratorStage.FLOWERS, waterside);
		waterside.add("cattail", 6, new GeneratorFlora.Builder()
				.with(BOPPlants.CATTAIL)
				.placeOn(BlockQueries.litFertileWaterside)
				.create()
		);
		waterside.add("reeds", 4, new GeneratorColumns.Builder()
				.generationAttempts(24)
				.placeOn(BlockQueries.litFertileWaterside)
				.with(Blocks.REEDS.getDefaultState())
				.minHeight(1)
				.maxHeight(3)
				.create()
		);
		
		// shroom
		GeneratorWeighted shroomGenerator = new GeneratorWeighted(4F);
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
