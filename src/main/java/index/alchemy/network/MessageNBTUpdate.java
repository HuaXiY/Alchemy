package index.alchemy.network;

import java.util.Iterator;

import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.annotation.Message;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.inventory.InventoryBauble;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Message(Side.CLIENT)
public class MessageNBTUpdate implements IMessage, IMessageHandler<MessageNBTUpdate, IMessage> {
	
	public static enum Type {
		ENTITY_DATA,
		ENTITY_BAUBLE_DATA
	}
	
	public Type type;
	public int id;
	public NBTTagCompound data;
	
	public MessageNBTUpdate() {}
	
	public MessageNBTUpdate(Type type, int id, NBTTagCompound data) {
		this.type = type;
		this.id = id;
		this.data = data;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(final MessageNBTUpdate message, MessageContext ctx) {
		AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
			@Override
			public void run(Phase phase) {
				World world = Minecraft.getMinecraft().theWorld;
				if (world == null)
					return;
				Entity entity = world.getEntityByID(message.id);
				if (entity != null)
					switch (message.type) {
						case ENTITY_BAUBLE_DATA:
							InventoryBauble inventory = entity.getCapability(AlchemyCapabilityLoader.bauble, null);
							inventory.readFromNBT(message.data);
						case ENTITY_DATA:
							updateNBT(entity.getEntityData(), message.data);
							break;
					}
			}
		}, 1);
		return null;
	}
	
	public void setNBT(ItemStack item, NBTTagCompound data) {
		if (item != null) {
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null)
				item.setTagCompound(nbt = new NBTTagCompound());
			updateNBT(nbt, data);
		}
	}
	
	public void updateNBT(NBTTagCompound nbt, NBTTagCompound data) {
		for (Iterator<String> iterator = data.getKeySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			nbt.setTag(key, data.getTag(key));
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = Type.values()[buf.readInt()];
		id = buf.readInt();
		data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		buf.writeInt(id);
		ByteBufUtils.writeTag(buf, data);
	}

}
