package index.alchemy.block;

import index.alchemy.api.annotation.Init;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED)
public class AlchemyBlockLoader {
	
	public static final Block 
			ice_temp = new BlockIceTemp(),
			ore_silver = new BlockOre("ore_silver", null, 0xFFFFFF,
					new BlockOre.OreGeneratorSetting(9, 4, 0, 40, OreGenEvent.GenerateMinable.EventType.GOLD));
	
}
