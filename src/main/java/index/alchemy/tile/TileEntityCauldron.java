package index.alchemy.tile;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Alway;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCauldron extends TileEntity implements ITickable {
	
	public static enum State {
		NULL,
		ALCHEMY,
		OVER
	}
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	private final LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	private boolean flag;
	
	private State state = State.NULL;
	private int time;
	
	private MaterialLiquid liquid;
	private boolean magic;
	
	public LinkedList<ItemStack> getContainer() {
		return container;
	}
	
	public State getState() {
		return state;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public MaterialLiquid getLiquid() {
		return liquid;
	}
	
	public void setLiquid(MaterialLiquid liquid) {
		this.liquid = liquid;
	}
	
	public boolean hasMagic() {
		return magic;
	}
	
	public void setMagic(boolean magic) {
		this.magic = magic;
	}

	@Override
	public void update() {
		if (Alway.isServer())
			if (liquid == Material.LAVA) {
				List<Entity> entitys = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).addCoord(0, 1, 0));
				for (Entity entity : entitys) {
					if (entity instanceof EntityItem) {
						entity.motionY = 0.2D;
						entity.motionX = (double) ((entity.rand.nextFloat() - entity.rand.nextFloat()) * 0.2F);
						entity.motionZ = (double) ((entity.rand.nextFloat() - entity.rand.nextFloat()) * 0.2F);
						entity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + entity.rand.nextFloat() * 0.4F);
					}
					entity.attackEntityFrom(DamageSource.lava, 4.0F);
					entity.setFire(15);
				}
			} else {
				List<EntityItem> entitys = worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).addCoord(0, 1, 0));
				for (EntityItem entity : entitys) {
					ItemStack item = entity.getEntityItem();
					for (ItemStack c_item : container) {
						if (c_item.getItem() == entity.getEntityItem().getItem() && c_item.stackSize < c_item.getMaxStackSize()) {
							flag = true;
							int change = Math.min(c_item.getMaxStackSize() - c_item.stackSize, item.stackSize);
							c_item.stackSize += change;
							item.stackSize -= change;
							if (item.stackSize <= 0)
								break;
						}
					}
					if (item.stackSize > 0 && container.size() < CONTAINER_MAX_ITEM) {
						flag = true;
						container.add(item.copy());
						item.stackSize = 0;
					}
				}
			}
		
		if (flag) {
			updateState();
			flag = false;
		}
	}
	
	public void updateState() {
		
	}
	
	public void onBlockBreak() {
		for (ItemStack item : container)
			InventoryHelper.spawnItemStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), item);
	}

}
