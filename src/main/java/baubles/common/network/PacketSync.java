package baubles.common.network;

import index.alchemy.api.annotation.Message;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.network.MessageBaubleNBTUpdate;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Omega
@Message(Side.CLIENT)
public class PacketSync extends MessageBaubleNBTUpdate {
	
	public PacketSync() { }
	
	public PacketSync(EntityPlayer player, int slotId) {
		super(player.getEntityId(), player.getCapability(AlchemyCapabilityLoader.bauble, null).getUpdateNBT(slotId));
	}
	
}
