package index.alchemy.client;

import index.alchemy.api.IRegister;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.Constants;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AlchemyKeyBinding extends KeyBinding implements IRegister {

	public AlchemyKeyBinding(String description, int keyCode) {
		super(description, keyCode, Constants.MOD_ID);
		register();
	}

	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}
