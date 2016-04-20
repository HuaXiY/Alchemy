package index.alchemy.client;

import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.IRegister;
import net.minecraft.client.settings.KeyBinding;

public class AlchemyKeyBinding extends KeyBinding implements IRegister {

	public AlchemyKeyBinding(String description, int keyCode, String category) {
		super(description, keyCode, category);
		register();
	}

	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}
