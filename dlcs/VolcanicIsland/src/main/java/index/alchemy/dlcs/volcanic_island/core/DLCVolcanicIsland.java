package index.alchemy.dlcs.volcanic_island.core;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Premise;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.LoaderState.ModState;
import scala.actors.threadpool.Arrays;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.dlcs.volcanic_island.core.DLCVolcanicIsland.*;

import org.apache.commons.lang3.ArrayUtils;

@Premise(MOD_ID)
@Init(state = ModState.CONSTRUCTED)
@DLC(name = NAME, version = VERSION)
public class DLCVolcanicIsland {
	
	public static final String NAME = "VolcanicIsland", VERSION = "0.0.1";
	
	public static void init() {
		float[] floats = ArrayUtils.toPrimitive(new Float[0]);
		System.out.println(NAME + " - Init");
	}

}
