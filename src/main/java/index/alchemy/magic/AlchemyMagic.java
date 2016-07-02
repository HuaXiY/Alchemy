package index.alchemy.magic;

import index.alchemy.api.IEntityLivingAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AlchemyMagic implements IEntityLivingAction, INBTSerializable<NBTTagCompound> {
	
	public static final String NBT_KEY_STRENGTH = "strength";
	
	protected float strength;
	
	public boolean hasStrength() {
		return true;
	}
	
	public float getStrength() {
		return strength;
	}
	
	public <T> T setStrength(float strength) {
		this.strength = strength;
		return (T) this;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat(NBT_KEY_STRENGTH, strength);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		strength = nbt.getFloat(NBT_KEY_STRENGTH);
	}

}