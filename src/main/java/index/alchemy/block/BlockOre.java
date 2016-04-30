package index.alchemy.block;

import java.util.Random;

import index.alchemy.client.IColorBlock;
import index.alchemy.core.IOreDictionary;
import index.alchemy.util.Tool;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOre extends AlchemyBlock implements IColorBlock, IOreDictionary {
	
	public static final Random RANDOM = new Random();
	
	private Item drop;
	private int drop_num, min_xp, max_xp, color;
	private boolean drop_fortune;
	
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
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess access, BlockPos pos, int index) {
				return index == 0 ? color : -1;
			}
		};
	}

	@Override
	public String getNameInOreDictionary() {
		return Tool._toUp(getRegistryName().getResourcePath());
	}
	
	@Override
	public ItemStack getItemStackInOreDictionary() {
		return new ItemStack(this);
	}
	
	public BlockOre(String name, Item drop, int color) {
		this(name, drop, 1, 0, 0, color, false);
	}
	
	public BlockOre(String name, Item drop, int drop_num, int min_xp, int max_xp, int color, boolean drop_fortune) {
		super(name, Material.ROCK, "ore");
		this.drop = drop;
		this.drop_num = drop_num;
		this.min_xp = min_xp;
		this.max_xp = max_xp;
		this.color = color;
		this.drop_fortune = drop_fortune;
	}

}
