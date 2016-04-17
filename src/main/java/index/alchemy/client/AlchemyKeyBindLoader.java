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
public class AlchemyKeyBindLoader {
	
	public static final KeyBinding
			key_space_ring = new KeyBinding("key.space_ring", Keyboard.KEY_R, Constants.MODID),
			key_space_ring_pickup = new KeyBinding("key.space_ring_pickup", Keyboard.KEY_C, Constants.MODID);
	
	public static void init() {}

}