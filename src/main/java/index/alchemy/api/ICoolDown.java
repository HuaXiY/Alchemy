package index.alchemy.api;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICoolDown {
	
	int getMaxCD();
	
	@SideOnly(Side.CLIENT)
	int getResidualCD();
	
	@SideOnly(Side.CLIENT)
	boolean isCDOver();
	
	@SideOnly(Side.CLIENT)
	void setResidualCD(int cd);
	
	@SideOnly(Side.CLIENT)
	void restartCD();
	
	@SideOnly(Side.CLIENT)
	int getRenderID();
	
	@SideOnly(Side.CLIENT)
	default void renderCD(int x, int y, int w, int h) { }

}