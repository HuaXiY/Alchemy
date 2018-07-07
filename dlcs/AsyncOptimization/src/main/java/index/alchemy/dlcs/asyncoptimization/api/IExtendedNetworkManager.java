package index.alchemy.dlcs.asyncoptimization.api;

import java.util.Optional;

import javax.annotation.Nullable;

import index.alchemy.api.ICastable;
import index.alchemy.api.annotation.Patch;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.IThreadListener;

public interface IExtendedNetworkManager extends ICastable<NetworkManager> {
	
	@Patch("net.minecraft.network.NetworkManager")
	static class Patch$NetworkManager extends NetworkManager implements IExtendedNetworkManager {

		@Patch.Exception
		public Patch$NetworkManager(EnumPacketDirection packetDirection) {
			super(packetDirection);
		}
		
	}
	
	default boolean isThreadRelated() {
		return cast().getNetHandler() instanceof NetHandlerPlayServer;
	}
	
	@Nullable
	default IThreadListener getThreadListener() {
		return Optional.ofNullable(cast().getNetHandler())
				.filter(NetHandlerPlayServer.class::isInstance)
				.map(NetHandlerPlayServer.class::cast)
				.map(handler -> handler.player)
				.map(player -> player.world)
				.filter(IThreadListener.class::isInstance)
				.map(IThreadListener.class::cast)
				.orElse(null);
	}
	
	default void safeProcessReceivedPackets() {
		IThreadListener listener = getThreadListener();
		if (listener != null)
			listener.addScheduledTask(cast()::processReceivedPackets);
		else
			cast().processReceivedPackets();
	}

}
