package index.alchemy.entity;

import net.minecraft.util.DamageSource;
import index.alchemy.api.IRegister;

public class AlchemyDamageSource extends DamageSource implements IRegister {

	public AlchemyDamageSource(String type) {
		super(type);
		register();
	}

}
