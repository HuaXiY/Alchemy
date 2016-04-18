package index.alchemy.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;

public class Elemix {
	
	public static boolean blockCanToIce(Block block) {
		return block == Blocks.AIR || block instanceof BlockTallGrass || block instanceof BlockSnow || block instanceof BlockLiquid;
	}
	
}
