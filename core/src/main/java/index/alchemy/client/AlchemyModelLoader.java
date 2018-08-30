package index.alchemy.client;

import index.alchemy.api.annotation.Init;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.core.AlchemyConstants.*;

@SideOnly(Side.CLIENT)
@Init(state = ModState.CONSTRUCTED)
public class AlchemyModelLoader {
	
	public static void init() {
		OBJLoader.INSTANCE.addDomain(MOD_ID);
	}

}
