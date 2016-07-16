package index.alchemy.client.fx.update;

import java.awt.Color;
import java.util.Iterator;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FXARGBIteratorUpdate implements IFXUpdate {
	
	protected Iterator<Color> iterator;
	
	public FXARGBIteratorUpdate(Iterator<Color> iterator) {
		this.iterator = iterator;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateFX(AlchemyFX fx, long tick) {
		if (iterator.hasNext()) {
			Color color = iterator.next();
			fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		} else
			fx.removeFXUpdate(this);
	}
	
}