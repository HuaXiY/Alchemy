package index.alchemy.entity;

import net.minecraft.util.DamageSource;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.IRegister;

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
