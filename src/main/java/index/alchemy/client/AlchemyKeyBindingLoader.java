package index.alchemy.client;

import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@Init(state = ModState.PREINITIALIZED)
public class AlchemyKeyBindingLoader {
	
	public static final KeyBinding
			key_space_ring_open = new AlchemyKeyBinding("key.space_ring_open", Keyboard.KEY_R, Constants.MOD_ID),
			key_space_ring_pickup = new AlchemyKeyBinding("key.space_ring_pickup", Keyboard.KEY_C, Constants.MOD_ID);
	
	public static void init() {}

}