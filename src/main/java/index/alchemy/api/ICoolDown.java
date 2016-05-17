package index.alchemy.api;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICoolDown {
	
	public int getMaxCD();
	
	@SideOnly(Side.CLIENT)
	public int getResidualCD();
	
	@SideOnly(Side.CLIENT)
	public int getRenderID();
	
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h);

}