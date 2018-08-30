package index.alchemy.api.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerDropItemEvent extends PlayerEvent {
	
	public final ItemStack stack;
	public final boolean dropAll;
	
	public PlayerDropItemEvent(EntityPlayer player, ItemStack stack, boolean dropAll) {
		super(player);
		this.stack = stack;
		this.dropAll = dropAll;
	}
	
}
