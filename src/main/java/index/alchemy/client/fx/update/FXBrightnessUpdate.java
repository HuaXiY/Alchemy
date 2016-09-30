package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;

public class FXBrightnessUpdate implements IFXUpdate {
	
	protected int brightness;
	
	public FXBrightnessUpdate(int brightness) {
		this.brightness = brightness;
	}

	@Override
	public void updateFX(AlchemyFX fx, long tick) {
		fx.setBrightness(brightness);
		fx.removeFXUpdate(this);
	}

}
