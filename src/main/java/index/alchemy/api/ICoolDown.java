package index.alchemy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICoolDown {
	
	public int getMaxCD();
	
	public int getResidualCD(EntityPlayer player);
	
	@SideOnly(Side.CLIENT)
	public int getRenderID();
	
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h);

}