package index.alchemy.network;

import index.alchemy.api.Alway;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyNetworkHandler {
	public static final SimpleNetworkWrapper networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MODID);
	private static int register = -1;

	public static void init() {
		registerMessage(MessageOpenGui.class, Side.SERVER);
		registerMessage(MessageSpaceRingPickUp.class, Side.SERVER);
		registerMessage(MessageAlacrityCallback.class, Side.SERVER);
		registerMessage(MessageParticle.class, Side.CLIENT);
	}

	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz, Side side) {
		networkWrapper.registerMessage(clazz, clazz, ++register, side);
	}
	
	public static <T extends IMessage, R extends IMessageHandler<T, IMessage>> void registerMessage(Class<T> message, Class<R> handle, Side side) {
		networkWrapper.registerMessage(handle, message, ++register, side);
	}
	
	public static void registerMessage(INetworkMessage<IMessage> handle) {
		networkWrapper.registerMessage(handle, handle.getMessageClass(), ++register, handle.getMessageSide());
	}
}