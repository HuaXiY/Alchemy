package index.alchemy.client.fx.update;

import index.alchemy.animation.ICycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;

public class FXScaleUpdate implements IFXUpdate {
	
	protected ICycle cycle;
	
	public FXScaleUpdate(ICycle cycle) {
		this.cycle = cycle;
	}

	@Override
	public void updateFX(AlchemyFX fx, long tick) {
		fx.setScaleF(cycle.next());
	}

}