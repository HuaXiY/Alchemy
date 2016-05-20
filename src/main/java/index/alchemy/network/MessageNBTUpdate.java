package index.alchemy.network;

import java.util.Iterator;

import index.alchemy.annotation.Message;
import index.alchemy.item.AlchemyItemBauble;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Message(Side.CLIENT)
public class MessageNBTUpdate implements IMessage, IMessageHandler<MessageNBTUpdate, IMessage> {
	
	public static enum Type {
		PLAYER_ENTITY_DATA,
		PLAYER_INVENTORY,
		PLAYER_BAUBLE
	}
	
	public Type type;
	public int id;
	public NBTTagCompound data;
	
	public MessageNBTUpdate() {}
	
	public MessageNBTUpdate(Type type, NBTTagCompound data) {
		this(type, -1, data);
	}
	
	public MessageNBTUpdate(Type type, int id, NBTTagCompound data) {
		this.type = type;
		this.id = id;
		this.data = data;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageNBTUpdate message, MessageContext ctx) {
		switch (message.type) {
			case PLAYER_ENTITY_DATA:
				updateNBT(Minecraft.getMinecraft().thePlayer.getEntityData(), message.data);
				break;
			case PLAYER_INVENTORY:
				setNBT(Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(message.id), message.data);
				break;
			case PLAYER_BAUBLE:
				setNBT(AlchemyItemBauble.getBauble(Minecraft.getMinecraft().thePlayer, message.id), message.data);
				break;
		}
		return null;
	}
	
	public void setNBT(ItemStack item, NBTTagCompound data) {
		if (item != null) {
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null)
				item.setTagCompound(nbt = new NBTTagCompound());
			updateNBT(nbt, data);
		}
	}
	
	public void updateNBT(NBTTagCompound nbt, NBTTagCompound data) {
		for (Iterator<String> iterator = data.getKeySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			nbt.setTag(key, data.getTag(key));
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = Type.values()[buf.readInt()];
		id = buf.readInt();
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		buf.writeInt(id);
		ByteBufUtils.writeTag(buf, data);
	}

}
