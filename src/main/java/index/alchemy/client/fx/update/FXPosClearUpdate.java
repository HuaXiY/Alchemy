package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;

@Omega
public class FXPosClearUpdate implements IFXUpdate {

	@Override
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.setPosition(0, 0, 0);
		return true;
	}

}
