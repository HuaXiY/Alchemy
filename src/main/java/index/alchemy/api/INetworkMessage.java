package index.alchemy.api;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public interface INetworkMessage<T extends IMessage> extends IMessageHandler<T, IMessage> {
	
	public static interface Client<T extends IMessage> extends INetworkMessage<T> {
		
		public Class<T> getClientMessageClass();
		
	}
	
	public static interface Server<T extends IMessage> extends INetworkMessage<T> {
		
		public Class<T> getServerMessageClass();
		
	}
	
}