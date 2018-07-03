package index.alchemy.api;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Patch;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IShareableSerializable {
	
	@Patch("net.minecraft.entity.Entity")
	public static class Patch$Entity implements IShareableSerializable {

		@Override
		public Type getSerializableType() {
			return Type.ENTITY;
		}

	}
	
	@Patch("net.minecraft.tileentity.TileEntity")
	public static class Patch$TileEntity implements IShareableSerializable {

		@Override
		public Type getSerializableType() {
			return Type.TILE_ENTITY;
		}

	}
	
	enum Type {
		
		ENTITY {
			
			@Override
			void serialize(Object object, ByteBuf buf) {
				buf.writeInt(Entity.class.cast(object).getEntityId());
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			Entity deserialize(ByteBuf buf) {
				return Minecraft.getMinecraft().world.getEntityByID(buf.readInt());
			}
			
		}, TILE_ENTITY {
			
			@Override
			void serialize(Object object, ByteBuf buf) {
				new PacketBuffer(buf).writeBlockPos(TileEntity.class.cast(object).getPos());
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			Object deserialize(ByteBuf buf) {
				return Minecraft.getMinecraft().world.getTileEntity(new PacketBuffer(buf).readBlockPos());
			}
			
		};
		
		abstract void serialize(Object object, ByteBuf buf);
		
		@SideOnly(Side.CLIENT)
		abstract Object deserialize(ByteBuf buf);
		
	}
	
	Type getSerializableType();
	
	default void serialize(ByteBuf buf) {
		buf.writeByte(getSerializableType().ordinal());
		getSerializableType().serialize(this, buf);
	}
	
	@Nullable
	static Object deserialize(ByteBuf buf) {
		return Type.values()[buf.readByte()].deserialize(buf);
	}
	
}
