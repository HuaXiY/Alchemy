package index.alchemy.tile;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Alway;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class TileEntityCauldron extends AlchemyTileEntity implements ITickable {
	
	public static enum State {
		NULL,
		ALCHEMY,
		OVER
	}
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	private static final String 
			NBT_KEY_CONTAINER = "container",
			NBT_KEY_STATE = "state",
			NBT_KEY_TIME = "time",
			NBT_KEY_FLUID = "fluid",
			NBT_KEY_ALCHEMY = "alchemy";
	
	private final LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	private volatile boolean flag;
	
	private State state = State.NULL;
	private int time;
	
	private Fluid fluid;
	/*  alchemy: 
	 *  	Magic Solvent volume -> alchemy & 4
	 *  	Glow Stone volume -> alchemy >> 4 & 4
	 *  	Red Stone volume -> alchemy >> 8 & 4
	 *  	Dragon's Breath volume -> alchemy >> 12 & 4
	 */
	private int alchemy;
	
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
	
	public Fluid getLiquid() {
		return fluid;
	}
	
	public void setLiquid(Fluid fluid) {
		this.fluid = fluid;
	}
	
	public void setAlchemy(int alchemy) {
		this.alchemy = alchemy;
	}
	
	public int getAlchemy() {
		return alchemy;
	}

	@Override
	public void update() {
		if (Alway.isServer())
			if (fluid == FluidRegistry.LAVA) {
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
		updateTracker();
	}
	
	public void onBlockBreak() {
		for (ItemStack item : container)
			InventoryHelper.spawnItemStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), item);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		container.clear();
		container.addAll(Arrays.asList(NBTHelper.getItemStacksFormNBTList(compound.getTagList(NBT_KEY_CONTAINER, NBT.TAG_COMPOUND))));
		state = State.values()[compound.getInteger(NBT_KEY_STATE)];
		time = compound.getInteger(NBT_KEY_TIME);
		fluid = FluidRegistry.getFluid(compound.getString(NBT_KEY_FLUID));
		alchemy = compound.getInteger(NBT_KEY_ALCHEMY);
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag(NBT_KEY_CONTAINER, NBTHelper.getNBTListFormItemStacks(container.toArray(new ItemStack[container.size()])));
		compound.setInteger(NBT_KEY_STATE, state.ordinal());
		compound.setInteger(NBT_KEY_TIME, time);
		compound.setString(NBT_KEY_FLUID, Tool.isNullOr(FluidRegistry.getFluidName(fluid), ""));
		compound.setInteger(NBT_KEY_ALCHEMY, alchemy);
		return super.writeToNBT(compound);
	}

}
