package index.alchemy.magic;

import index.alchemy.api.IEntityLivingAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AlchemyMagic implements IEntityLivingAction, INBTSerializable<NBTTagCompound> {
	
	@Override
	public NBTTagCompound serializeNBT() {
		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {}

}