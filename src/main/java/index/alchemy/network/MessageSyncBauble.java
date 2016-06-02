package index.alchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import baubles.common.Baubles;
import baubles.common.lib.PlayerHandler;
import index.alchemy.annotation.Message;

@Message(Side.CLIENT)
public class MessageSyncBauble implements IMessage, IMessageHandler<MessageSyncBauble, IMessage> {
	
	public int slot;
	public int id;
	public ItemStack bauble;
	
	public MessageSyncBauble() {}
	
	public MessageSyncBauble(EntityLivingBase living, int slot) {
		this.slot = slot;
		// TODO this.bauble = PlayerHandler.getPlayerBaubles(player).getStackInSlot(slot);
		this.id = living.getEntityId();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(slot);
		buffer.writeInt(id);
		ByteBufUtils.writeItemStack(buffer, bauble);
	}

	@Override
	public void fromBytes(ByteBuf buffer) 
	{
		slot = buffer.readByte();
		id = buffer.readInt();
		bauble = ByteBufUtils.readItemStack(buffer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(final MessageSyncBauble message, MessageContext ctx) {		
		Minecraft.getMinecraft().addScheduledTask(new Runnable(){ 
			public void run() {
				World world = Minecraft.getMinecraft().theWorld;
				// TODO
				/*Entity p = world.getEntityByID(message.id);
				if (p !=null && p instanceof EntityPlayer) {
					PlayerHandler.getPlayerBaubles((EntityPlayer) p).stackList[message.slot]=message.bauble;
				}*/
			}
		});		
		return null;
	}
	
}
