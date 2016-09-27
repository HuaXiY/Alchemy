package baubles.common.lib;

import baubles.common.container.InventoryBaubles;
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
	public static InventoryBaubles getPlayerBaubles(EntityPlayer player) {
		return player.getCapability(AlchemyCapabilityLoader.bauble, null);
	}

}