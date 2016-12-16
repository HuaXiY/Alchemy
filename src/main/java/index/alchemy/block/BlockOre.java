package index.alchemy.block;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import index.alchemy.api.IColorItem;
import index.alchemy.api.IGenerator;
import index.alchemy.api.IOreDictionary;
import index.alchemy.api.annotation.Config;
import index.alchemy.config.AlchemyConfig;
import index.alchemy.util.Tool;
import index.alchemy.world.AlchemyWorldGenerator;
import index.project.version.annotation.Beta;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
public class BlockOre extends AlchemyBlockColor implements IOreDictionary, IGenerator, IColorItem {
	
	@Config(handle = AlchemyConfig.HANDLE_INT_ARRAY, category = AlchemyWorldGenerator.CATEGORY_GENERATOR, comment = "Ore generation exception dimension ids.")
	public static int not_generator_dimension_ids[] = { -1, 1 };
	
	public static class OreGeneratorSetting {
		
		public final int size, count, minH, maxH, dH;
		
		public boolean canGenerator(World world, BlockPos pos) {
			return true;
		}
		
		public OreGeneratorSetting(int size, int count, int minH, int maxH) {
			this.size = size;
			this.count = count;
			this.minH = minH;
			this.maxH = maxH;
			this.dH = maxH - minH;
		}
		
	}
	
	protected Item drop;
	protected int drop_num, min_xp, max_xp;
	protected boolean drop_fortune;
	
	protected OreGeneratorSetting setting; 
	protected WorldGenMinable generator;
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return drop;
	}

	@Override
	public int quantityDropped(Random random) {
		return drop_num;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return quantityDropped(random) + (drop_fortune ? random.nextInt(fortune + 1) : 0);
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
		return max_xp == 0 ? 0 : min_xp + RANDOM.nextInt(max_xp + fortune);
	}
	
	@Override
	public String getNameInOreDictionary() {
		return Tool._ToUpper(getRegistryName().getResourcePath());
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!ArrayUtils.contains(not_generator_dimension_ids, world.provider.getDimension()))
			for (int i = 0; i < setting.count; i++) {
				BlockPos pos = new BlockPos(chunkX * 16 + random.nextInt(16),
						random.nextInt(setting.dH) + setting.minH, chunkZ * 16 + random.nextInt(16));
				if (setting.canGenerator(world, pos))
					generator.generate(world, random, pos);
			}
	}

	@Override
	public int getWeight() {
		return 1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return new IItemColor() {
			
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				return color;
			}
			
		};
	}
	
	public BlockOre(String name, Item drop, int color, OreGeneratorSetting setting) {
		this(name, drop, 1, 0, 0, color, false, setting);
	}
	
	public BlockOre(String name, Item drop, int drop_num, int min_xp, int max_xp, int color, boolean drop_fortune, OreGeneratorSetting setting) {
		super(name, Material.ROCK, "ore", color);
		this.drop = drop == null ? Item.getItemFromBlock(this) : drop;
		this.drop_num = drop_num;
		this.min_xp = min_xp;
		this.max_xp = max_xp;
		this.drop_fortune = drop_fortune;
		this.setting = setting;
		this.generator = new WorldGenMinable(this.getDefaultState(), setting.size);
	}

}
