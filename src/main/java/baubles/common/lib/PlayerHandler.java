package baubles.common.lib;

import index.alchemy.capability.AlchemyCapabilityLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@Deprecated
public class PlayerHandler {

	@Deprecated
	public static void clearPlayerBaubles(EntityPlayer player) {
		IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		if (inventory != null)
			inventory.clear();
	}

	@Deprecated
	public static IInventory getPlayerBaubles(EntityPlayer player) {
		return player.getCapability(AlchemyCapabilityLoader.bauble, null);
	}

}
