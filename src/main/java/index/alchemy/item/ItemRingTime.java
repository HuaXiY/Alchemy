package index.alchemy.item;

import java.util.Iterator;

import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.AlchemyKeyBindingLoader;
import index.alchemy.client.render.ICoolDown;
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
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRingTime extends AlchemyItemRing implements IEventHandle, INetworkMessage<MessageTimeLeap>, ICoolDown {
	
	public static final int USE_CD = 20 * 20;
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
			if (isEquipmented(Minecraft.getMinecraft().thePlayer) &&
					Minecraft.getMinecraft().thePlayer.ticksExisted - Minecraft.getMinecraft().thePlayer.getEntityData().getInteger(NBT_KEY_CD) > USE_CD) {
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageTimeLeap());
				Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
				timeLeapOnClinet(Minecraft.getMinecraft().thePlayer);
			}
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
		AlchemyEventSystem.addInputHook(this);
		AlchemyEventSystem.addContinuedRunnable(new IIndexRunnable() {
			int flag = TimeSnapshot.SIZE / 2 - 1;
			@Override
			public boolean run(int index, Phase phase) {
				boolean result = false;
				if (iterator.hasNext())
					iterator.next().updatePlayerOnClient(player);
				if (result = index >= flag) {
					snapshot.setUpdate(true);
					AlchemyEventSystem.removeInputHook(ItemRingTime.this);
				}
				return result;
			}
		}, TimeSnapshot.SIZE / 2, Side.CLIENT);
	}
	
	public void timeLeapOnServer(final EntityPlayer player) {
		if (isEquipmented(player) && player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD) > USE_CD) {
			player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, TimeSnapshot.SIZE / 2, 3));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, TimeSnapshot.SIZE / 2, 3));
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
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getMaxCD() {
		return USE_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		return isEquipmented(Minecraft.getMinecraft().thePlayer) ? 
				Math.max(0, USE_CD - (Minecraft.getMinecraft().thePlayer.ticksExisted - Minecraft.getMinecraft().thePlayer.getEntityData().getInteger(NBT_KEY_CD))) : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public ItemRingTime() {
		super("ring_time", 0xFFFFFF);
	}

}
