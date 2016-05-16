package index.alchemy.entity;

import net.minecraft.util.DamageSource;
import index.alchemy.api.IRegister;
import index.alchemy.core.AlchemyInitHook;

public class AlchemyDamageSource extends DamageSource implements IRegister {

	public AlchemyDamageSource(String type) {
		super(type);
		register();
	}

	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}
