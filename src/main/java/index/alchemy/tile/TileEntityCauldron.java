package index.alchemy.tile;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.IAlchemyRecipe;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.annotation.FX;
import index.alchemy.client.color.ColorHelper;
import index.alchemy.client.fx.FXWisp;
import index.alchemy.client.fx.update.FXARGBIteratorUpdate;
import index.alchemy.client.fx.update.FXARGBUpdate;
import index.alchemy.client.fx.update.FXAgeUpdate;
import index.alchemy.client.fx.update.FXMotionUpdate;
import index.alchemy.client.fx.update.FXPosUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.core.AlchemyRegistry;
import index.alchemy.interacting.Elemix;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double3Float2Package;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

@FX.UpdateProvider
public class TileEntityCauldron extends AlchemyTileEntity implements ITickable {
	
	public static final String FX_KEY_GATHER = "fx_cauldron_gather", FX_KEY_HOVER = "fx_cauldron_hover";
	
	public static final int 
			fx_id_gather = FXUpdateHelper.getIdByName(FX_KEY_GATHER),
			fx_id_hover = FXUpdateHelper.getIdByName(FX_KEY_HOVER);
	
	@FX.UpdateMethod(FX_KEY_GATHER)
	public static List<IFXUpdate> getFXUpdateGather(int[] args) {
		List<IFXUpdate> result = new LinkedList<IFXUpdate>();
		int i = 1, 
			max_age = Tool.getSafe(args, i++, 1),
			scale = Tool.getSafe(args, i++, 1);
		result.add(new FXAgeUpdate(max_age));
		result.add(new FXPosUpdate(0, 6, 3));
		result.add(new FXMotionUpdate(
				new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setMin(-.5F).setMax(.5F),
				new StdCycle().setLenght(max_age).setMax(-.2F),
				new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setNow(max_age / 6).setMin(-.5F).setMax(.5F)));
		result.add(new FXARGBIteratorUpdate(ColorHelper.ahsbStep(new Color(0x66, 0xCC, 0xFF), Color.RED, max_age, true, true, false)));
		result.add(new FXScaleUpdate(new StdCycle().setLenght(max_age).setMin(scale / 1000F).setMax(scale / 100F)));
		return result;
	}
	
	@FX.UpdateMethod(FX_KEY_HOVER)
	public static List<IFXUpdate> getFXUpdateHover(int[] args) {
		List<IFXUpdate> result = new LinkedList<IFXUpdate>();
		int i = 1, 
			max_age = Tool.getSafe(args, i++, 1),
			scale = Tool.getSafe(args, i++, 1),
			color = Tool.getSafe(args, i++, 0x66CCFF);
		result.add(new FXAgeUpdate(max_age));
		result.add(new FXARGBUpdate(color | 0x88 << 24));
		result.add(new FXScaleUpdate(new StdCycle().setLenght(max_age).setMin(scale / 1000F).setMax(scale / 100F)));
		return result;
	}
	
	public static enum State { NULL, ALCHEMY, OVER }
	
	public static final int CONTAINER_MAX_ITEM = 6;
	
	protected static final int
			UPDATE_STATE_ID = 0;
	
	protected static final String 
			NBT_KEY_CONTAINER = "container",
			NBT_KEY_STATE = "state",
			NBT_KEY_RECIPE = "recipe",
			NBT_KEY_TIME = "time",
			NBT_KEY_LIQUID = "liquid",
			NBT_KEY_ALCHEMY = "alchemy";
	
	protected final LinkedList<ItemStack> container = new LinkedList<ItemStack>();
	protected volatile boolean flag, check;
	
	protected State state = State.NULL;
	protected IAlchemyRecipe recipe;
	protected int time;
	
	protected FluidTank tank = new FluidTank(Fluid.BUCKET_VOLUME) {
		
		{
			setTileEntity(TileEntityCauldron.this);
		}
		
		public Fluid getFluidType() {
			return getFluid() != null ? getFluid().getFluid() : null;
		}
		
		public void setFluid(FluidStack stack) {
			Fluid fluid = getFluidType();
			super.setFluid(stack);
			if (getFluidType() != fluid)
				update();
		};
		
		@Override
		public int fillInternal(FluidStack resource, boolean doFill) {
			Fluid fluid = getFluidType();
			int result = super.fillInternal(resource, doFill);
			if (getFluidType() != fluid)
				update();
			return result;
		}
		
		public FluidStack drainInternal(int maxDrain, boolean doDrain) {
			Fluid fluid = getFluidType();
			FluidStack result = super.drainInternal(maxDrain, doDrain);
			if (getFluidType() != fluid)
				update();
			return result;
		};
		
		public void update() {
			if (worldObj != null && pos != null) {
				worldObj.checkLight(pos);
				worldObj.updateComparatorOutputLevel(pos, Blocks.CAULDRON);
			}
		}
		
	};
	
	
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
		FluidStack stack = tank.getFluid();
		if (stack != null)
			if (stack.getFluid() == FluidRegistry.WATER)
				return stack.amount / 333;
			else
				return -1;
		return 0;
	}
	
	public void setLevel(int level) {
		if (level == 0)
			tank.setFluid(null);
		else if (getLevel() > -1) {
			if (tank.getFluid() == null)
				tank.setFluid(new FluidStack(FluidRegistry.WATER, 0));
			tank.getFluid().amount = (int) (level / 3F * 1000);
		}
	}
	
	@Nullable
	public IBlockState getLiquid() {
		return tank.getFluid() == null ? null : tank.getFluid().getFluid().getBlock().getDefaultState();
	}
	
	public FluidTank getTank() {
		return tank;
	}
	
	public void setAlchemy(int alchemy) {
		this.alchemy = alchemy;
	}
	
	public int getAlchemy() {
		return alchemy;
	}
	
	public void onContainerChange() {
		state = State.NULL;
		flag = true;
		check = false;
		recipe = null;
		
	}

	@Override
	public void update() {
		if (Always.isServer()) {
			List<EntityItem> entitys = worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).setMaxY(pos.getY() + .35));
			for (EntityItem entity : entitys) {
				ItemStack item = entity.getEntityItem();
				while (item.stackSize > 0 && container.size() < CONTAINER_MAX_ITEM) {
					ItemStack itemStack = item.splitStack(1);
					container.add(itemStack);
					onContainerChange();
				}
				if (item.stackSize == 0)
					entity.setDead();
			}
			
			if (!container.isEmpty() && tank.getFluid() != null && tank.getFluid().getFluid() == FluidRegistry.LAVA) {
				container.clear();
				onContainerChange();
				List<Double3Float2Package> d3f2ps = new LinkedList<>();
				d3f2ps.add(new Double3Float2Package(pos.getX() + .5F, pos.getY() + .8F, pos.getZ() + .5F,
						.4F, 2F + worldObj.rand.nextFloat() * .4F));
				AlchemyNetworkHandler.playSound(SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.NEUTRAL,
						AABBHelper.getAABBFromBlockPos(pos, AlchemyNetworkHandler.getSoundRange()), worldObj, d3f2ps);
			}
			
			if (!check && state == State.NULL && container.size() > 0) {
				recipe = AlchemyRegistry.findRecipe(container);
				check = true;
			}
			
			if (recipe != null && state != State.OVER) {
				if (worldObj.isAirBlock(pos.up()) && Elemix.blockIsHeatSource(worldObj, pos.down()) &&
						tank.getFluid() != null && tank.getFluid().getFluid() == recipe.getAlchemyFluid() &&
						tank.getFluidAmount() == Fluid.BUCKET_VOLUME) {
					time++;
					checkStateChange(State.ALCHEMY);
				} else {
					time = 0;
					checkStateChange(State.NULL);
				}
				if (state == State.ALCHEMY && time >= recipe.getAlchemyTime()) {
					container.clear();
					container.add(recipe.getAlchemyResult(worldObj, pos));
					state = State.OVER;
					tank.setFluid(null);
					time = 0;
					flag = true;
					worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, pos.getX(), pos.getY(), pos.getZ(), true));
				}
			}
		
			if (flag)
				updateTracker();
		} else {
			if (recipe != null) {
				if (worldObj.getWorldTime() % 3 == 0)
					worldObj.spawnParticle(FXWisp.Info.type, false,
							pos.getX() + worldObj.rand.nextFloat(),
							pos.getY() + 1 + worldObj.rand.nextFloat(),
							pos.getZ() + worldObj.rand.nextFloat(),
							worldObj.rand.nextGaussian() * .05, worldObj.rand.nextFloat() * .15, worldObj.rand.nextGaussian() * .05,
							new int[]{ fx_id_hover, 20, (int) (100 + worldObj.rand.nextFloat() * 50), recipe.getAlchemyColor() });
				if (state == State.ALCHEMY)
					worldObj.spawnParticle(FXWisp.Info.type, false,
							pos.getX() + .5 - 2 + worldObj.rand.nextFloat() * 4,
							pos.getY() + worldObj.rand.nextFloat(),
							pos.getZ() + .5 - 2 + worldObj.rand.nextFloat() * 4,
							0, 0, 0, new int[]{ fx_id_gather, 60, 200 });
			}
		}
	}
	
	public void checkStateChange(State state) {
		if (this.state != state) {
			this.state = state;
			worldObj.addBlockEvent(pos, Blocks.CAULDRON, UPDATE_STATE_ID, state.ordinal());
		}
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		switch (id) {
			case UPDATE_STATE_ID:
				state = Tool.getSafe(State.values(), type, state);
				return true;
			default:
				return super.receiveClientEvent(id, type);
		}
	}
	
	public void updateTracker() {
		super.updateTracker();
		flag = false;
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
		recipe = AlchemyRegistry.findRecipe(nbt.getString(NBT_KEY_RECIPE));
		time = nbt.getInteger(NBT_KEY_TIME);
		alchemy = nbt.getInteger(NBT_KEY_ALCHEMY);
		tank.readFromNBT(nbt);
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag(NBT_KEY_CONTAINER, NBTHelper.getNBTListFormItemStacks(container.toArray(new ItemStack[container.size()])));
		nbt.setInteger(NBT_KEY_STATE, state.ordinal());
		nbt.setString(NBT_KEY_RECIPE, recipe != null ? recipe.getAlchemyName().toString() : "");
		nbt.setInteger(NBT_KEY_TIME, time);
		nbt.setInteger(NBT_KEY_ALCHEMY, alchemy);
		tank.writeToNBT(nbt);
		return super.writeToNBT(nbt);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == EnumFacing.UP ||
				super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing == EnumFacing.UP)
			return (T) tank;
		return super.getCapability(capability, facing);
	}

}
