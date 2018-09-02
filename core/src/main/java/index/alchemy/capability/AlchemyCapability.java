package index.alchemy.capability;

import index.alchemy.api.ICapability;
import index.alchemy.api.IRegister;
import index.project.version.annotation.Omega;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import static index.alchemy.util.$.$;

@Omega
public abstract class AlchemyCapability<T> implements ICapability<T>, IRegister {
    
    public static final String TYPE = "Capability";
    
    @Override
    public T call() throws Exception {
        return $(getDataClass(), "new");
    }
    
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
        return instance instanceof INBTSerializable ? ((INBTSerializable<?>) instance).serializeNBT() : null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
        if (instance instanceof INBTSerializable)
            ((INBTSerializable<NBTBase>) instance).deserializeNBT(nbt);
    }
    
    public AlchemyCapability() {
        register();
    }
    
}
