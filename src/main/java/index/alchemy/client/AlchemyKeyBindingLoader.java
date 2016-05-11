package index.alchemy.client;

import org.lwjgl.input.Keyboard;

import index.alchemy.core.Init;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Init(state = ModState.PREINITIALIZED)
public class AlchemyKeyBindingLoader {
	
	public static final KeyBinding
			key_space_ring_open = new AlchemyKeyBinding("key.space_ring_open", Keyboard.KEY_R),
			key_space_ring_pickup = new AlchemyKeyBinding("key.space_ring_pickup", Keyboard.KEY_C),
			key_time_ring_leap = new AlchemyKeyBinding("key.time_ring_leap", Keyboard.KEY_F);
	
	public static void init() {}

}