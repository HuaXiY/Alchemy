package index.alchemy.client.fx.update;

import index.alchemy.api.ICycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FXMotionUpdate implements IFXUpdate {
	
	protected ICycle xCycle, yCycle, zCycle;
	
	public FXMotionUpdate(ICycle xCycle, ICycle yCycle, ICycle zCycle) {
		this.xCycle = xCycle;
		this.yCycle = yCycle;
		this.zCycle = zCycle;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateFX(AlchemyFX fx, long tick) {
		fx.setMotionX(xCycle.next());
		fx.setMotionY(yCycle.next());
		fx.setMotionZ(zCycle.next());
	}

}