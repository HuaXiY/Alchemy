package baubles.common.container;

import baubles.api.cap.IBaublesItemHandler;
import index.alchemy.api.AlchemyBaubles;
import index.alchemy.inventory.InventoryBauble;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@Omega
public class SlotBauble extends InventoryBauble.SlotBauble {

	public SlotBauble(EntityPlayer player, IBaublesItemHandler itemHandler, int index, int x, int y) {
		super(player, (IInventory) itemHandler, AlchemyBaubles.getAllBaubles().get(index), index, x, y);
	}
	
}
