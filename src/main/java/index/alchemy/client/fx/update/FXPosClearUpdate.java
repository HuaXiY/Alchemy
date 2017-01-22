package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
public class FXPosClearUpdate implements IFXUpdate {

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		fx.setPosition(0, 0, 0);
		return true;
	}

}
