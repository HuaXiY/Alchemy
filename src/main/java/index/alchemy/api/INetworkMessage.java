package index.alchemy.api;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public interface INetworkMessage<T extends IMessage & IMessageHandler<T, IMessage>> {
	
	public static interface Client<T extends IMessage & IMessageHandler<T, IMessage>> extends INetworkMessage<T> {
		
		Class<T> getClientMessageClass();
		
	}
	
	public static interface Server<T extends IMessage & IMessageHandler<T, IMessage>> extends INetworkMessage<T> {
		
		Class<T> getServerMessageClass();
		
	}
	
}