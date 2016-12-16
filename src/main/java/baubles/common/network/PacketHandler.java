package baubles.common.network;

import index.alchemy.network.AlchemyNetworkHandler;
import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Omega
public class PacketHandler {
	
	public static final SimpleNetworkWrapper INSTANCE = AlchemyNetworkHandler.network_wrapper;

}
