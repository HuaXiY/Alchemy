package index.alchemy.container;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.annotation.Proxy;
import index.alchemy.api.annotation.Texture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@Proxy(ContainerPlayer.class)
public class ContainerInventoryBauble {
	
	public static final String EMPTY_NAME[] = SlotBauble.class.getAnnotation(Texture.class).value();
	
	@Texture({
		"alchemy:items/empty_ring",
		"alchemy:items/empty_amulet",
		"alchemy:items/empty_belt"
	})
	public class SlotBauble extends Slot {
		
		private final BaubleType type;
		private final EntityLivingBase living;
		
		public SlotBauble(EntityLivingBase living, IInventory inventory, BaubleType type, int index, int x, int y) {
			super(inventory, index, x, y);
			this.living = living;
			this.type = type;
			setBackgroundName(EMPTY_NAME[type.ordinal()]);
		}
		
		public BaubleType getType() {
			return type;
		}
		
		@Override
		public boolean isItemValid(ItemStack item) {
			return item != null && item.getItem() != null &&
					item.getItem() instanceof IBauble && 
				   ((IBauble) item.getItem()).getBaubleType(item) == type &&
				   ((IBauble) item.getItem()).canEquip(item, living);
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return getStack() != null &&
				   ((IBauble) getStack().getItem()).canUnequip(getStack(), player);
		}
		
		@Override
		public int getSlotStackLimit() {
			return 1;
		}
		
	}

}
