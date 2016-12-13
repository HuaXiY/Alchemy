package baubles.common.network;

import index.alchemy.network.AlchemyNetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketHandler {
	
	public static final SimpleNetworkWrapper INSTANCE = AlchemyNetworkHandler.network_wrapper;

}
