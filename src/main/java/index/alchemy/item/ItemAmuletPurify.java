package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Alway;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.config.AlchemyConfig;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.item.ItemAmuletPurify.MessagePurifyCallback;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6Package;
import index.alchemy.potion.AlchemyPotion;
import index.alchemy.util.AABBHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAmuletPurify extends AlchemyItemAmulet implements ICoolDown, IEventHandle, INetworkMessage.Client<MessagePurifyCallback> {
	
	public static final int INTERVAL = 20 * 20, MAX_AIR = 300;
	public static final String NBT_KEY_CD = "amulet_purify";
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Alway.isServer()) {
			if (living.ticksExisted - living.getEntityData().getInteger(NBT_KEY_CD) > INTERVAL) {
				boolean flag = false;
				for (PotionEffect effect : living.getActivePotionEffects())
					if (effect.getPotion().isBadEffect()) {
						living.removePotionEffect(effect.getPotion());
						flag = true;
					}
				if (flag) {
					List<Double6Package> d6p = new LinkedList<Double6Package>();
					for (int i = 0; i < 9; i++)
						d6p.add(new Double6Package(living.posX - 1 + living.rand.nextDouble() * 2, living.posY + 1, living.posZ - 1 + living.rand.nextDouble() * 2, 0D, 0D, 0D));
					AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.WATER_SPLASH, AABBHelper.getAABBFromEntity(living, AlchemyConfig.getParticleRange()), living.worldObj, d6p);
					living.getEntityData().setInteger(NBT_KEY_CD, living.ticksExisted);
					if (living instanceof EntityPlayerMP)
						AlchemyNetworkHandler.network_wrapper.sendTo(new MessagePurifyCallback(), (EntityPlayerMP) living);
				}
			}
			if (living.isInWater()) {
				living.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, AlchemyPotion.NOT_FLASHING_TIME));
				living.setAir(MAX_AIR);
			}
		}
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		event.getEntity().getEntityData().removeTag(NBT_KEY_CD);
	}
	
	public static class MessagePurifyCallback implements IMessage {
		@Override
		public void fromBytes(ByteBuf buf) {}

		@Override
		public void toBytes(ByteBuf buf) {}
	}
	
	@Override
	public Class<MessagePurifyCallback> getClientMessageClass() {
		return MessagePurifyCallback.class;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessagePurifyCallback message, MessageContext ctx) {
		restartCD();
		return null;
	}
	
	@Override
	public int getMaxCD() {
		return INTERVAL;
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
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}

	public ItemAmuletPurify() {
		super("amulet_purify", 0x66CCFF);
	}

}
