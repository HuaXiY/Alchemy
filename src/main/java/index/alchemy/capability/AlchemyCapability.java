package index.alchemy.capability;

import index.alchemy.api.ICapability;
import index.alchemy.api.IRegister;
import index.alchemy.core.AlchemyInitHook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public abstract class AlchemyCapability<T> implements ICapability<T>, IRegister {
	
	@Override
	public T call() throws Exception {
		return getDataClass().newInstance();
	}

	@Override
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
		return null;
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) { }

	public AlchemyCapability() {
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}

}