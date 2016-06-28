package index.alchemy.client.fx.update;

import index.alchemy.animation.ICycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;

public class FXAlphaUpdate implements IFXUpdate {
	
	protected final ICycle cycle;
	
	public FXAlphaUpdate(ICycle cycle) {
		this.cycle = cycle;
	}

	@Override
	public void updateFX(AlchemyFX fx, long tick) {
		fx.setAlphaF(cycle.next());
	}

}