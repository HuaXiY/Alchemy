package index.alchemy.block;

import index.alchemy.api.annotation.Init;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyBlockLoader {
	
	public static final Block 
			ice_temp = new BlockIceTemp(),
			ore_silver = new BlockOre("ore_silver", null, 0xFFFFFF, new BlockOre.OreGeneratorSetting(9, 4, 0, 40));
	
}
