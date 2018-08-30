package index.alchemy.interacting.minecraft;

import java.io.IOException;

import index.alchemy.api.annotation.Patch;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Patch("net.minecraft.network.play.server.SPacketRespawn")
public class Patch$SPacketRespawn extends SPacketRespawn {
	
	public int dimensionTypeId = -1;
	
	public Patch$SPacketRespawn(int dimensionId, EnumDifficulty difficulty, WorldType worldType, GameType gameMode) {
		super(dimensionId, difficulty, worldType, gameMode);
		dimensionTypeId = DimensionManager.getProviderType(dimensionId).ordinal();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		writePacketData(buf);
		buf.writeVarInt(dimensionTypeId);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		readPacketData(buf);
		if (buf.readableBytes() > 0)
			dimensionTypeId = buf.readVarInt();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void processPacket(INetHandlerPlayClient handler) {
		PacketThreadUtil.checkThreadAndEnqueue(this, handler, Minecraft.getMinecraft());
		if (dimensionTypeId != -1) {
			if (DimensionManager.isDimensionRegistered(dimensionID))
				DimensionManager.unregisterDimension(dimensionID);
			DimensionManager.registerDimension(dimensionID, DimensionType.values()[dimensionTypeId]);
		}
		processPacket(handler);
	}

}
