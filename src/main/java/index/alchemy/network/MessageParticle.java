package index.alchemy.network;

import index.alchemy.api.annotation.Message;
import index.alchemy.core.AlchemyEventSystem;
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
	public Double6IntArrayPackage d6iaps[];
	
	public MessageParticle() { }
	
	public MessageParticle(int id, Double6IntArrayPackage... d6iaps) {
		this.id = id;
		len = d6iaps.length;
		this.d6iaps = d6iaps;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(final MessageParticle message, MessageContext ctx) {
		AlchemyEventSystem.addDelayedRunnable(p -> {
			Double6IntArrayPackage d6iaps[] = message.d6iaps;
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			EnumParticleTypes type = EnumParticleTypes.getParticleFromId(message.id);
			for (int i = 0; i < d6iaps.length; i++)
				player.worldObj.spawnParticle(type, false, d6iaps[i].x, d6iaps[i].y, d6iaps[i].z,
						d6iaps[i].ox, d6iaps[i].oy, d6iaps[i].oz, d6iaps[i].args);
		}, 1);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		len = buf.readInt();
		d6iaps = new Double6IntArrayPackage[len];
		for (int i = 0; i < len; i++) {
			d6iaps[i] = new Double6IntArrayPackage(buf.readDouble(), buf.readDouble(), buf.readDouble(),
					buf.readDouble(), buf.readDouble(), buf.readDouble());
			int size = buf.readInt();
			d6iaps[i].args = new int[size];
			for (int k = 0; k < size; k++)
				d6iaps[i].args[k] = buf.readInt();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(len);
		for (int i = 0; i < len; i++) {
			buf.writeDouble(d6iaps[i].x);
			buf.writeDouble(d6iaps[i].y);
			buf.writeDouble(d6iaps[i].z);
			buf.writeDouble(d6iaps[i].ox);
			buf.writeDouble(d6iaps[i].oy);
			buf.writeDouble(d6iaps[i].oz);
			buf.writeInt(d6iaps[i].args.length);
			for (int arg : d6iaps[i].args)
				buf.writeInt(arg);
		}
	}

}
