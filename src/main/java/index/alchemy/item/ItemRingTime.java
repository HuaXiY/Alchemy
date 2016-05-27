package index.alchemy.item;

import java.util.Iterator;

import org.lwjgl.input.Keyboard;

import index.alchemy.annotation.KeyEvent;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot.TimeNode;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingTime.MessageTimeLeap;
import index.alchemy.network.AlchemyNetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRingTime extends AlchemyItemRing implements IInputHandle, INetworkMessage.Server<MessageTimeLeap>, ICoolDown {
	
	public static final int USE_CD = 20 * 20;
	public static final String NBT_KEY_CD = "cd_ring_time", KEY_DESCRIPTION = "key.time_ring_leap";
	
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
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				new AlchemyKeyBinding(KEY_DESCRIPTION, Keyboard.KEY_V)
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(KEY_DESCRIPTION)
	public void onKeyTimeLeapPressed(KeyBinding binding) {
		if (isCDOver()) {
			AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageTimeLeap());
			Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
			timeLeapOnClinet(Minecraft.getMinecraft().thePlayer);
		}
	}
	
	public static class MessageTimeLeap implements IMessage {
		@Override
		public void fromBytes(ByteBuf buf) {}

		@Override
		public void toBytes(ByteBuf buf) {}
	}
	
	@Override
	public Class<MessageTimeLeap> getServerMessageClass() {
		return MessageTimeLeap.class;
	}
	
	@Override
	public IMessage onMessage(MessageTimeLeap message, MessageContext ctx) {
		timeLeapOnServer(ctx.getServerHandler().playerEntity);
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void timeLeapOnClinet(final EntityPlayer player) {
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			AlchemyEventSystem.addInputHook(this);
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					player.addPotionEffect(new PotionEffect(MobEffects.SPEED, TimeSnapshot.SIZE / 2, 3));
				}
			}, 0, Side.SERVER);
			final Iterator<TimeNode> iterator = snapshot.list.iterator();
			AlchemyEventSystem.addContinuedRunnable(new IContinuedRunnable() {
				@Override
				public boolean run(Phase phase) {
					if (iterator.hasNext())
						iterator.next().updatePlayerOnClient(player);
					if (!iterator.hasNext()) {
						snapshot.setUpdate(true);
						AlchemyEventSystem.removeInputHook(ItemRingTime.this);
						return true;
					}
					return false;
				}
			}, Side.CLIENT);
		}
	}
	
	public void timeLeapOnServer(final EntityPlayer player) {	
		final TimeSnapshot snapshot = player.getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (isEquipmented(player) && snapshot.isUpdate()) {
			snapshot.setUpdate(false);
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					player.addPotionEffect(new PotionEffect(MobEffects.SPEED, TimeSnapshot.SIZE / 2, 3));
					player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, TimeSnapshot.SIZE / 2, 3));
				}
			}, 0, Side.SERVER);
			final Iterator<TimeNode> iterator = snapshot.list.iterator();
			AlchemyEventSystem.addContinuedRunnable(new IContinuedRunnable() {
				@Override
				public boolean run(Phase phase) {
					boolean result = false;
					if (iterator.hasNext())
						iterator.next().updatePlayerOnServer(player);
					if (!iterator.hasNext()) {
						snapshot.setUpdate(true);
						return true;
					}
					return false;
				}
			}, Side.SERVER);
		}
	}
	
	@Override
	public int getMaxCD() {
		return USE_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return isEquipmented(player) ? 
				Math.max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		Minecraft.getMinecraft().thePlayer.getEntityData().setInteger(NBT_KEY_CD, Minecraft.getMinecraft().thePlayer.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}
	
	public ItemRingTime() {
		super("ring_time", 0xFFFFFF);
	}

}
