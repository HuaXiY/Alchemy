package index.alchemy.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public interface INetworkMessage<T extends IMessage> extends IMessageHandler<T, IMessage> {
	
	public Class<T> getMessageClass();
	
	public Side getMessageSide();
	
}