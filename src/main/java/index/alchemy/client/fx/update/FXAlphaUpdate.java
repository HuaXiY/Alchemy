package index.alchemy.client.fx.update;

import index.alchemy.api.ICycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FXAlphaUpdate implements IFXUpdate {
	
	protected final ICycle cycle;
	
	public FXAlphaUpdate(ICycle cycle) {
		this.cycle = cycle;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.setAlphaF(cycle.next());
		return false;
	}

}