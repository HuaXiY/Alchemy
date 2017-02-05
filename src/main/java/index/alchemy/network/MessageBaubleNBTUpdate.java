package index.alchemy.network;

import index.alchemy.api.annotation.Message;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.Always;
import index.alchemy.util.NBTHelper;
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
public class MessageBaubleNBTUpdate implements IMessage, IMessageHandler<MessageBaubleNBTUpdate, IMessage> {
	
	public int id;
	public NBTTagCompound data;
	
	public MessageBaubleNBTUpdate() { }
	
	public MessageBaubleNBTUpdate(int id, NBTTagCompound data) {
		this.id = id;
		this.data = data;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageBaubleNBTUpdate message, MessageContext ctx) {
		AlchemyEventSystem.addDelayedRunnable(p -> {
			Entity entity = Always.findEntityFormClientWorld(message.id);
			if (entity != null) {
				InventoryBauble inventory = entity.getCapability(AlchemyCapabilityLoader.bauble, null);
				if (inventory != null && message.data != null)
					if (message.data.hasKey(InventoryBauble.UPDATE_INDEX_NBT_KEY))
						inventory.setInventorySlotContents(message.data.getInteger(InventoryBauble.UPDATE_INDEX_NBT_KEY),
								NBTHelper.getItemStackFormNBT((NBTTagCompound) message.data.getTag(InventoryBauble.CONTENTS)));
					else
						inventory.deserializeNBT(message.data);
			}
		}, 1);
		return null;
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
