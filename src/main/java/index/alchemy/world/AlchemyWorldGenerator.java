package index.alchemy.world;

import index.alchemy.api.IGenerator;
import index.alchemy.api.IRegister;
import index.project.version.annotation.Omega;

@Omega
public abstract class AlchemyWorldGenerator implements IGenerator, IRegister {
	
	public static final String CATEGORY_GENERATOR = "generator";
	
	private int level;
	
	@Override
	public int getWeight() {
		return level;
	}
	
	protected AlchemyWorldGenerator(int level) {
		this.level = level;
		register();
	}
	
}
