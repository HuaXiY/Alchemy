package index.alchemy.network;

import index.alchemy.api.annotation.Message;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Message(Side.CLIENT)
public class MessageParticle implements IMessage, IMessageHandler<MessageParticle, IMessage> {
	
	public int id, len;
	public Double6IntArrayPackage d6iap[];
	
	public MessageParticle() {}
	
	public MessageParticle(int id, Double6IntArrayPackage... d6iap) {
		this.id = id;
		len = d6iap.length;
		this.d6iap = d6iap;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageParticle message, MessageContext ctx) {
		Double6IntArrayPackage d6iap[] = message.d6iap;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		EnumParticleTypes type = EnumParticleTypes.getParticleFromId(message.id);
		for (int i = 0; i < d6iap.length; i++)
			player.worldObj.spawnParticle(type, false, d6iap[i].x, d6iap[i].y, d6iap[i].z, d6iap[i].ox, d6iap[i].oy, d6iap[i].oz, d6iap[i].args);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		len = buf.readInt();
		d6iap = new Double6IntArrayPackage[len];
		for (int i = 0; i < len; i++) {
			d6iap[i] = new Double6IntArrayPackage(buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readDouble(), buf.readDouble(), buf.readDouble());
			int size = buf.readInt();
			d6iap[i].args = new int[size];
			for (int k = 0; k < size; k++)
				d6iap[i].args[k] = buf.readInt();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(len);
		for (int i = 0; i < len; i++) {
			buf.writeDouble(d6iap[i].x);
			buf.writeDouble(d6iap[i].y);
			buf.writeDouble(d6iap[i].z);
			buf.writeDouble(d6iap[i].ox);
			buf.writeDouble(d6iap[i].oy);
			buf.writeDouble(d6iap[i].oz);
			buf.writeInt(d6iap[i].args.length);
			for (int arg : d6iap[i].args)
				buf.writeInt(arg);
		}
	}

}
