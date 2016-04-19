package index.alchemy.network;

import java.util.List;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemBauble;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.item.IItemInventory;
import index.alchemy.item.ItemInventory;
import index.alchemy.item.ItemRingSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSpaceRingPickUp implements IMessage, IMessageHandler<MessageSpaceRingPickUp, IMessage> {
	
	@Override
	public IMessage onMessage(MessageSpaceRingPickUp message, MessageContext ctx) {
		AlchemyItemLoader.ring_space.pickup(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
	
}
