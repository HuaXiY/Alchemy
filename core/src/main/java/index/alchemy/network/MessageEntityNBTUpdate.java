package index.alchemy.network;

import java.util.Iterator;

import index.alchemy.api.annotation.Message;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Message(Side.CLIENT)
public class MessageEntityNBTUpdate implements IMessage, IMessageHandler<MessageEntityNBTUpdate, IMessage> {
	
	public int id;
	public NBTTagCompound data;
	
	public MessageEntityNBTUpdate() { }
	
	public MessageEntityNBTUpdate(int id, NBTTagCompound data) {
		this.id = id;
		this.data = data;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageEntityNBTUpdate message, MessageContext ctx) {
		AlchemyEventSystem.addDelayedRunnable(p -> {
			Entity entity = Always.findEntityFormClientWorld(message.id);
			if (entity != null)
				updateNBT(entity.getEntityData(), message.data);
		}, 1);
		return null;
	}
	
	public void updateNBT(NBTTagCompound nbt, NBTTagCompound data) {
		for (Iterator<String> iterator = data.getKeySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			nbt.setTag(key, data.getTag(key));
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		ByteBufUtils.writeTag(buf, data);
	}

}
