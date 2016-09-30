package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;

public class FXAgeUpdate implements IFXUpdate {
	
	protected int age;
	
	public FXAgeUpdate(int age) {
		this.age = age;
	}

	@Override
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.setMaxAge(age);
		return true;
	}

}
