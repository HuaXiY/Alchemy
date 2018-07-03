package index.alchemy.network.datasync;

import java.io.IOException;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.SuppressFBWarnings;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Init(state = ModState.INITIALIZED)
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class DataSerializers extends net.minecraft.network.datasync.DataSerializers {
	
	public static abstract class DataSerializer<T> implements net.minecraft.network.datasync.DataSerializer<T> {
		
		@Hook.Provider
		public static abstract class RemoteReference<T> extends DataSerializer<T> {
			
			public T changeReference(T value) {
				try {
					PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
					write(buffer, value);
					return read(buffer);
				} catch (ClassCastException | IOException e) { throw new RuntimeException(e); }
			}
			
			@SideOnly(Side.CLIENT)
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Hook("net.minecraft.network.play.server.SPacketEntityMetadata#func_148833_a")
			public static void processPacket(SPacketEntityMetadata packet, INetHandlerPlayClient handler) {
				if (FMLCommonHandler.instance().getMinecraftServerInstance() != null)
					packet.dataManagerEntries.replaceAll(entry -> entry.getKey().getSerializer() instanceof RemoteReference ?
							new EntityDataManager.DataEntry(entry.getKey(), RemoteReference.class.cast(entry.getKey().getSerializer()).changeReference(entry.getValue())) : entry);
			}
			
		}
		
		public DataSerializer() {
			DataSerializers.registerSerializer(this);
		}
		
		@Override
		public DataParameter<T> createKey(int id) {
			return new DataParameter<T>(id, this);
		}

	}
	
	public static final DataSerializer<Entity> ENTITY = new DataSerializer.RemoteReference<Entity>() {
		
 	 	public void write(PacketBuffer buf, Entity value) {
 	 	 	buf.writeInt(value == null ? -1 : value.getEntityId());
 	 	}
 	 	
 	 	public Entity read(PacketBuffer buf) throws IOException {
 	 	 	return Minecraft.getMinecraft().world.getEntityByID(buf.readInt());
 	 	}

		@Override
		public Entity copyValue(Entity value) {
			return EntityList.createEntityFromNBT(value.serializeNBT(), value.world);
		}
 	 	
 	};
 	
}
