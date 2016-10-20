package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FXPosUpdate implements IFXUpdate {
	
	protected double offsetX, offsetY, offsetZ;
	
	public FXPosUpdate(double offsetX, double offsetY, double offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.moveEntity(fx.getPosX() + offsetX, fx.getPosY() + offsetY, fx.getPosZ() + offsetZ);
		return true;
	}

}
