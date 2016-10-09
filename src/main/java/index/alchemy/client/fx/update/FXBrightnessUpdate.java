package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FXBrightnessUpdate implements IFXUpdate {
	
	protected int brightness;
	
	public FXBrightnessUpdate(int brightness) {
		this.brightness = brightness;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.setBrightness(brightness);
		return false;
	}

}
