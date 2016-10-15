package baubles.api;

import javax.annotation.Nullable;

import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * @author Azanor, Mickeyxiami
 */
public class BaublesApi  {
	
	/**
	 * Retrieves the baubles inventory capability handler for the supplied player
	 */
	@Nullable
	public static IBaublesItemHandler getBaublesHandler(EntityPlayer player) {
		return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
	}
		
	/**
	 * Retrieves the baubles capability handler wrapped as a IInventory for the supplied player
	 */
	@Nullable
	@Deprecated
	public static IInventory getBaubles(EntityPlayer player) {
		return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
	}
	
}