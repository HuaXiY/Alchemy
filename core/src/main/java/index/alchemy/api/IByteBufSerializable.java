package index.alchemy.api;

import net.minecraft.nbt.NBTBase;

import io.netty.buffer.ByteBuf;

public interface IByteBufSerializable<T extends NBTBase> {
    
    void serialize(ByteBuf buf);
    
    void deserialize(ByteBuf buf);
    
}
