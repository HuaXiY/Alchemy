package index.alchemy.network;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.gui.GUIID;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenGui implements IMessage, IMessageHandler<MessageOpenGui, IMessage> {
	
	public int id;
	
	public MessageOpenGui() {}
	
	public MessageOpenGui(int id) {
		this.id = id;
	}

	@Override
	public IMessage onMessage(MessageOpenGui message, MessageContext ctx) {
		EntityPlayer player = ctx.getServerHandler().playerEntity;
		player.openGui(AlchemyModLoader.instance(), message.id, player.worldObj,
				(int) player.posX, (int) player.posY, (int) player.posZ);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
	}

}
