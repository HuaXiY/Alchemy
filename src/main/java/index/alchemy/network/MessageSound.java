package index.alchemy.network;

import index.alchemy.api.annotation.Message;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Message(Side.CLIENT)
public class MessageSound implements IMessage, IMessageHandler<MessageSound, IMessage> {
	
	public int len;
	public String id, category;
	public Double3Float2Package d3f2p[];
	
	public MessageSound() {}
	
	public MessageSound(String id, String category, Double3Float2Package... d3f2p) {
		this.id = id;
		this.category = category;
		this.d3f2p = d3f2p;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageSound message, MessageContext ctx) {
		Double3Float2Package d3f2p[] = message.d3f2p;
		SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(message.id));
		SoundCategory category = SoundCategory.getByName(message.category);
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		for (int i = 0; i < d3f2p.length; i++)
			player.worldObj.playSound(d3f2p[i].x, d3f2p[i].y, d3f2p[i].z, sound, category, d3f2p[i].a, d3f2p[i].b, true);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = ByteBufUtils.readUTF8String(buf);
		category = ByteBufUtils.readUTF8String(buf);
		len = buf.readInt();
		d3f2p = new Double3Float2Package[len];
		for (int i = 0; i < len; i++) {
			d3f2p[i] = new Double3Float2Package(buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readFloat(), buf.readFloat());
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, id);
		ByteBufUtils.writeUTF8String(buf, category);
		buf.writeInt(len);
		for (int i = 0; i < len; i++) {
			buf.writeDouble(d3f2p[i].x);
			buf.writeDouble(d3f2p[i].y);
			buf.writeDouble(d3f2p[i].z);
			buf.writeFloat(d3f2p[i].a);
			buf.writeFloat(d3f2p[i].b);
		}
	}

}
