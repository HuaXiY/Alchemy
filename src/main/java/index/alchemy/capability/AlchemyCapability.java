package index.alchemy.capability;

import java.util.concurrent.Callable;

import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.IRegister;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public abstract class AlchemyCapability<T> implements IStorage<T>, Callable<T>, IRegister {
	
	public abstract Class<T> getDataClass();
	
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

	@Override
	public void register() {
		AlchemyInitHook.init(this);
	}
	
	public AlchemyCapability() {
		register();
	}

}
