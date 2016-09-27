package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Always;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.item.ItemAmuletPurify.MessagePurifyCallback;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
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

import static java.lang.Math.*;

public class ItemAmuletPurify extends AlchemyItemAmulet implements ICoolDown, IEventHandle, INetworkMessage.Client<MessagePurifyCallback> {
	
	public static final int INTERVAL = 20 * 20, MAX_AIR = 300;
	public static final String NBT_KEY_CD = "amulet_purify";
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer()) {
			if (living.ticksExisted - living.getEntityData().getInteger(NBT_KEY_CD) > INTERVAL) {
				List<PotionEffect> effects = new LinkedList<PotionEffect>();
				for (PotionEffect effect : living.getActivePotionEffects())
					if (effect.getPotion().isBadEffect())
						effects.add(effect);
				if (effects.size() > 0) {
					for (PotionEffect effect : effects)
						living.removePotionEffect(effect.getPotion());
					List<Double6IntArrayPackage> d6iap = new LinkedList<Double6IntArrayPackage>();
					for (int i = 0; i < 9; i++)
						d6iap.add(new Double6IntArrayPackage(living.posX - 1 + living.rand.nextDouble() * 2, living.posY + 1,
								living.posZ - 1 + living.rand.nextDouble() * 2, 0D, 0D, 0D));
					AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.WATER_SPLASH, AABBHelper.getAABBFromEntity(living,
							AlchemyNetworkHandler.getParticleRange()), living.worldObj, d6iap);
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
				max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
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
