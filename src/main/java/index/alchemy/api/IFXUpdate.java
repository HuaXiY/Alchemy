package index.alchemy.api;

import index.alchemy.client.fx.AlchemyFX;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IFXUpdate {
	
	@SideOnly(Side.CLIENT)
	boolean updateFX(AlchemyFX fx, long tick);

}