package index.alchemy.network;

import index.alchemy.api.Alway;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyNetworkHandler {
	public static SimpleNetworkWrapper networkWrapper;
	private static int register = -1;

	public static void init() {
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MODID);
		
		registerPacket(MessageOpenGui.class, Side.SERVER);
		registerPacket(MessageSpaceRingPickUp.class, Side.SERVER);
		registerPacket(MessageAlacrityCallback.class, Side.SERVER);
		
		if (Alway.isClient()) {
			registerPacket(MessageParticle.class, Side.CLIENT);
		}
	}

	private static void registerPacket(Class clazz, Side side) {
		networkWrapper.registerMessage(clazz, clazz, ++register, side);
	}
}
