package baubles.common.network;

import index.alchemy.api.annotation.Message;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.network.MessageNBTUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Message(Side.CLIENT)
public class PacketSync extends MessageNBTUpdate {
	
	public PacketSync() { }
	
	public PacketSync(EntityPlayer player, int slotId) {
		super(Type.ENTITY_BAUBLE_DATA, player.getEntityId(),
				player.getCapability(AlchemyCapabilityLoader.bauble, null).getUpdateNBT(slotId));
	}
	
}
