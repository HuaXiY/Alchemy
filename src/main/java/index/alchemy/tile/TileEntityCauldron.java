package index.alchemy.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.annotation.FX;
import index.alchemy.client.color.ColorHelper;
import index.alchemy.client.fx.update.FXARGBIteratorUpdate;
import index.alchemy.client.fx.update.FXMotionUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.util.Always;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;

@FX.UpdateProvider
public class TileEntityCauldron extends AlchemyTileEntity implements ITickable {
	
	public static final String FX_KEY_GATHER = "cauldron_gather";
	
	@FX.UpdateMethod(FX_KEY_GATHER)
	public static List<IFXUpdate> getFXUpdateGather(int[] args) {
		List<IFXUpdate> result = new LinkedList<IFXUpdate>();
		int i = 0, 
			max_age = Tool.getSafe(args, i++, 0),
			scale = Tool.getSafe(args, i++, 1);
		result.add(new FXMotionUpdate(
				new StdCycle().setLoop(true).setRotation(true).setMax(0.5F),
				new StdCycle().setMax(0.3F),
				new StdCycle().setLoop(true).setRotation(true).setMax(0.5F)));
		result.add(new FXARGBIteratorUpdate(ColorHelper.ahsbStep(Color.RED, new Color(0x66, 0xCC, 0xFF, 0x22), max_age, true, true, false)));
		result.add(new FXScaleUpdate(new StdCycle().setMax(scale / 100F)));
		return result;
	}
	
	public static enum State { NULL, ALCHEMY, OVER }
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	protected static final String 
			NBT_KEY_CONTAINER = "container",
			NBT_KEY_STATE = "state",
			NBT_KEY_TIME = "time",
			NBT_KEY_LIQUID = "liquid",
			NBT_KEY_ALCHEMY = "alchemy";
	
	protected final LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	protected volatile boolean flag;
	
	protected State state = State.NULL;
	protected int time, level;
	
	@Nullable
	protected IBlockState liquid = Blocks.LAVA.getDefaultState();
	/*  alchemy: 
	 *  	Magic Solvent volume	 -> alchemy & 4
	 *  	Glow Stone volume		 -> alchemy >> 4 & 4
	 *  	Red Stone volume 		 -> alchemy >> 8 & 4
	 *  	Dragon's Breath volume	 -> alchemy >> 12 & 4
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
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Nullable
	public IBlockState getLiquid() {
		return liquid;
	}
	
	public void setLiquid(IBlockState liquid) {
		this.liquid = liquid;
	}
	
	public void setAlchemy(int alchemy) {
		this.alchemy = alchemy;
	}
	
	public int getAlchemy() {
		return alchemy;
	}

	@Override
	public void update() {
		if (Always.isServer()) {
			List<Entity> entitys = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
			for (Entity entity : entitys) {
				if (entity instanceof EntityItem) {
					ItemStack item = ((EntityItem) entity).getEntityItem();
					for (ItemStack c_item : container) {
						if (c_item.getItem() == ((EntityItem) entity).getEntityItem().getItem() && c_item.stackSize < c_item.getMaxStackSize()) {
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
	}
	
	public void updateState() {
		updateTracker();
	}
	
	public void onBlockBreak() {
		for (ItemStack item : container)
			InventoryHelper.spawnItemStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), item);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		container.clear();
		container.addAll(Arrays.asList(NBTHelper.getItemStacksFormNBTList(nbt.getTagList(NBT_KEY_CONTAINER, NBT.TAG_COMPOUND))));
		state = State.values()[nbt.getInteger(NBT_KEY_STATE)];
		time = nbt.getInteger(NBT_KEY_TIME);
		liquid = Block.getStateById(nbt.getInteger(NBT_KEY_LIQUID));
		alchemy = nbt.getInteger(NBT_KEY_ALCHEMY);
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag(NBT_KEY_CONTAINER, NBTHelper.getNBTListFormItemStacks(container.toArray(new ItemStack[container.size()])));
		nbt.setInteger(NBT_KEY_STATE, state.ordinal());
		nbt.setInteger(NBT_KEY_TIME, time);
		nbt.setInteger(NBT_KEY_LIQUID, Block.getStateId(liquid));
		nbt.setInteger(NBT_KEY_ALCHEMY, alchemy);
		return super.writeToNBT(nbt);
	}

}
