package index.alchemy.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;

public interface IByteBufSerializable<T extends NBTBase> {
	
	void serialize(ByteBuf buf);
	
    void deserialize(ByteBuf buf);

}
