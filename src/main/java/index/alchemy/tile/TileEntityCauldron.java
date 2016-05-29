package index.alchemy.tile;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCauldron extends TileEntity implements ITickable {
	
	public static enum State {
		NULL,
		ALCHEMY,
		OVER
	}
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	private LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	private boolean container_change;
	
	private State state;
	private int alchemy_time;

	@Override
	public void update() {
		List<EntityItem> entitys = worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.up(1)));
		for (EntityItem entity : entitys) {
			ItemStack item = entity.getEntityItem();
			for (ItemStack c_item : container) {
				if (c_item.getItem() == entity.getEntityItem().getItem() && c_item.stackSize < c_item.getMaxStackSize()) {
					container_change = true;
					int change = Math.min(c_item.getMaxStackSize() - c_item.stackSize, item.stackSize);
					c_item.stackSize += change;
					item.stackSize -= change;
					if (item.stackSize <= 0)
						break;
				}
			}
			if (item.stackSize > 0 && container.size() < CONTAINER_MAX_ITEM) {
				container_change = true;
				container.add(item.copy());
				item.stackSize = 0;
			}
		}
		
		if (container_change) {
			updateState();
			container_change = false;
		}
	}
	
	public void updateState() {
		
	}

}
