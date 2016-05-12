package index.alchemy.item;

import java.util.Iterator;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.AlchemyKeyBindingLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.core.IEventHandle;
import index.alchemy.core.IIndexRunnable;
import index.alchemy.core.IPhaseRunnable;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
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
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRingTime extends AlchemyItemRing implements IEventHandle, INetworkMessage<MessageTimeLeap> {
	
	public static final int USE_CD = 20 * 30;
	public static final String NBT_KEY_CD = "time_leap";
	
	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {
		if (living instanceof EntityPlayer)
			living.getCapability(AlchemyCapabilityLoader.time_leap, null).list.clear();
	}
	
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
	
	@SideOnly(Side.CLIENT)
	public void timeLeapOnClinet(final EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		final Iterator<TimeNode> iterator = snapshot.list.iterator();
		snapshot.setUpdate(false);
		AlchemyEventSystem.addKeyInputHook(this);
		AlchemyEventSystem.addContinuedRunnable(new IIndexRunnable() {
			int flag = TimeSnapshot.SIZE / 2 - 1;
			@Override
			public boolean run(int index, Phase phase) {
				boolean result = false;
				if (iterator.hasNext())
					iterator.next().updatePlayerOnClient(player);
				if (result = index >= flag) {
					snapshot.setUpdate(true);
					AlchemyEventSystem.removeKeyInputHook(ItemRingTime.this);
				}
				return result;
			}
		}, TimeSnapshot.SIZE / 2, Side.CLIENT);
	}
	
	public void timeLeapOnServer(final EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		final Iterator<TimeNode> iterator = snapshot.list.iterator();
		snapshot.setUpdate(false);
		AlchemyEventSystem.addContinuedRunnable(new IIndexRunnable() {
			int flag = TimeSnapshot.SIZE / 2 - 1;
			@Override
			public boolean run(int index, Phase phase) {
				boolean result = false;
				if (iterator.hasNext())
					iterator.next().updatePlayerOnServer(player);
				if (result = index >= flag)
					snapshot.setUpdate(true);
				return result;
			}
		}, TimeSnapshot.SIZE / 2, Side.SERVER);
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
