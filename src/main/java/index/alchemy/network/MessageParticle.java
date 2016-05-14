package index.alchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageParticle implements IMessage, IMessageHandler<MessageParticle, IMessage> {
	
	public int id, len;
	public SDouble6Package d6p[];
	
	public MessageParticle() {}
	
	public MessageParticle(int id, SDouble6Package... d6p) {
		this.id = id;
		len = d6p.length;
		this.d6p = d6p;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageParticle message, MessageContext ctx) {
		SDouble6Package d6p[] = message.d6p;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		EnumParticleTypes type = EnumParticleTypes.getParticleFromId(message.id);
		for (int i = 0; i < d6p.length; i++)
			player.worldObj.spawnParticle(type, d6p[i].x, d6p[i].y, d6p[i].z, d6p[i].ox, d6p[i].oy, d6p[i].oz);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		len = buf.readInt();
		d6p = new SDouble6Package[len];
		for (int i = 0; i < len; i++) {
			d6p[i] = new SDouble6Package(buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readDouble(), buf.readDouble(), buf.readDouble());
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(len);
		for (int i = 0; i < len; i++) {
			buf.writeDouble(d6p[i].x);
			buf.writeDouble(d6p[i].y);
			buf.writeDouble(d6p[i].z);
			buf.writeDouble(d6p[i].ox);
			buf.writeDouble(d6p[i].oy);
			buf.writeDouble(d6p[i].oz);
		}
	}

}
