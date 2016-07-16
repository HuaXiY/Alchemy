package index.alchemy.interacting;

import index.alchemy.api.Alway;
import index.alchemy.api.annotation.Init;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Init(state = ModState.INITIALIZED)
public class Characteristic {
	
	public static void init() {
		GameRegistry.registerFuelHandler(Alway.getFuelHandler(new ItemStack(Items.BLAZE_POWDER), 20 * 30));
	}

}