package index.alchemy.world;

import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.IRegister;

public abstract class AlchemyWorldGenerator implements IGenerator, IRegister {
	
	private int level;
	
	@Override
	public int getWeight() {
		return level;
	}
	
	protected AlchemyWorldGenerator(int level) {
		this.level = level;
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}
	
}
