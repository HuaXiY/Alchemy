package index.alchemy.item;

import java.util.Iterator;

import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.AlchemyKeyBindingLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.core.IIndexRunnable;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingTime.MessageTimeLeap;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.INetworkMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRingTime extends AlchemyItemRing implements IEventHandle, INetworkMessage<MessageTimeLeap> {
	
	public static final int USE_CD = 20 * 30;
	public static final String NBT_KEY_CD = "time_leap";
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (living instanceof EntityPlayer)
			living.getCapability(AlchemyCapabilityLoader.time_leap, null).updateTimeNode((EntityPlayer) living);
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleKeyInput(KeyInputEvent event) {
		if (AlchemyKeyBindingLoader.key_time_ring_leap.isPressed()) {
			System.out.println("press");
			AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageTimeLeap());
			timeLeapOnClinet(Minecraft.getMinecraft().thePlayer);
			/*if (isEquipmented(Minecraft.getMinecraft().thePlayer) &&
					Minecraft.getMinecraft().thePlayer.ticksExisted - Minecraft.getMinecraft().thePlayer.getEntityData().getInteger(NBT_KEY_CD) > USE_CD) {
				System.out.println("send");
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageTimeLeap());
				Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
			}*/
		}
	}
	
	public static class MessageTimeLeap implements IMessage {
		@Override
		public void fromBytes(ByteBuf buf) {}

		@Override
		public void toBytes(ByteBuf buf) {}
	}
	
	@Override
	public Class<MessageTimeLeap> getMessageClass() {
		return MessageTimeLeap.class;
	}
	
	@Override
	public Side getMessageSide() {
		return Side.SERVER;
	}
	
	@Override
	public IMessage onMessage(MessageTimeLeap message, MessageContext ctx) {
		timeLeapOnServer(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	public void timeLeapOnClinet(final EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		final Iterator<TimeNode> iterator = snapshot.list.iterator();
		snapshot.setUpdate(false);
		AlchemyEventSystem.addContinuedRunnable(new IIndexRunnable() {
			@Override
			public void run(int index) {
				if (iterator.hasNext())
					iterator.next().updatePlayerOnClient(player);
				if (index + 1 == TimeSnapshot.SIZE)
					snapshot.setUpdate(true);
			}
		}, TimeSnapshot.SIZE, Side.CLIENT);
	}
	
	public void timeLeapOnServer(EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		snapshot.list.getLast().updatePlayerOnServer(player);
		snapshot.setUpdate(false);
		AlchemyEventSystem.addDelayedRunnable(new Runnable() {
			@Override
			public void run() {
				snapshot.setUpdate(true);
			}
		}, TimeSnapshot.SIZE, Side.SERVER);
		/*
		System.out.println("leap");
		if (isEquipmented(player) && player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD) > USE_CD) {
			System.out.println("cd-ok");
			TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
			if (snapshot != null) {
				System.out.println("non-null");
				snapshot.list.getLast().updatePlayer(player);
			}
			player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
		}*/
	}
	
	public ItemRingTime() {
		super("ring_time", 0xFFFFFF);
	}

}
