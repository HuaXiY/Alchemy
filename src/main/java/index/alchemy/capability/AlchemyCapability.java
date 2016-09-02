package index.alchemy.capability;

import index.alchemy.api.ICapability;
import index.alchemy.api.IRegister;
import index.alchemy.core.AlchemyInitHook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AlchemyCapability<T> implements ICapability<T>, IRegister {
	
	@Override
	public T call() throws Exception {
		return getDataClass().newInstance();
	}
	
	@Override
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
		return instance instanceof INBTSerializable ? ((INBTSerializable) instance).serializeNBT() : null;
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
		if (instance instanceof INBTSerializable)
			((INBTSerializable) instance).deserializeNBT(nbt);
	}

	public AlchemyCapability() {
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}