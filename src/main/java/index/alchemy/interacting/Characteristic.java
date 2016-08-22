package index.alchemy.interacting;

import index.alchemy.api.Always;
import index.alchemy.api.annotation.Init;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Init(state = ModState.INITIALIZED)
public class Characteristic {
	
	public static void init() {
		GameRegistry.registerFuelHandler(Always.getFuelHandler(new ItemStack(Items.BLAZE_POWDER), 20 * 30));
	}

}