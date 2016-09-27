package baubles.api;

import baubles.common.lib.PlayerHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.FMLLog;

/**
 * @author Azanor, Mickeyxiami
 */
public class BaublesApi 
{
	static boolean enable;
	static {
		try {
			enable = Class.forName("baubles.common.lib.PlayerHandler") != null;
		} catch (ClassNotFoundException e) { }
	}
	
	/**
	 * Retrieves the baubles inventory for the supplied player
	 */
	public static IInventory getBaubles(EntityPlayer player)
	{
		IInventory ot = null;
		
		if (enable) 
		    try
		    {
		        ot = PlayerHandler.getPlayerBaubles(player);
		    }
		    catch(Exception ex) 
		    { 
		    	FMLLog.warning("[Baubles API] Could not invoke baubles.common.lib.PlayerHandler method getPlayerBaubles");
		    }
	    
		return ot;
	}
	
}