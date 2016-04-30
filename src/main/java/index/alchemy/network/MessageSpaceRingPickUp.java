package index.alchemy.network;

import index.alchemy.item.AlchemyItemLoader;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSpaceRingPickUp implements IMessage, IMessageHandler<MessageSpaceRingPickUp, IMessage> {
	
	@Override
	public IMessage onMessage(MessageSpaceRingPickUp message, MessageContext ctx) {
		AlchemyItemLoader.ring_space.pickup(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
	
}
