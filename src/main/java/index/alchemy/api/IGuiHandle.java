package index.alchemy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGuiHandle {
	
	public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z);
	
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z);

}
