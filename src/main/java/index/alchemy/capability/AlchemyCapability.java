package index.alchemy.capability;

import index.alchemy.api.ICapability;
import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED)
public abstract class AlchemyCapability<T> implements ICapability<T>, IRegister {
	
	public static final String TYPE = "Capability";

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
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (Class<?> clazz : AlchemyModLoader.getInstance(TYPE))
			try {
				clazz.newInstance();
			} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}
	
}