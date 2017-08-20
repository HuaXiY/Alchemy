package index.alchemy.item;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IItemTemperature;
import index.alchemy.api.INetworkMessage;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.item.ItemAmuletPurify.MessagePurifyCallback;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

@Omega
public class ItemAmuletPurify extends AlchemyItemAmulet implements IEventHandle, INetworkMessage.Client<MessagePurifyCallback>,
		IItemTemperature, ICoolDown {
	
	public static final int INTERVAL = 20 * 16, MAX_AIR = 300;
	public static final float TEMPERATURE_RATE = 150F, TEMPERATURE_TARGET = -5F;
	public static final String NBT_KEY_CD = "amulet_purify";
	
	public static final ItemAmuletPurify type = null;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer())
			if (living.ticksExisted - living.getEntityData().getInteger(NBT_KEY_CD) > INTERVAL) {
				boolean flag = false;
				for (Iterator<PotionEffect> iterator = living.getActivePotionEffects().iterator(); iterator.hasNext();)
					if (iterator.next().getPotion().isBadEffect()) {
						flag = true;
						iterator.remove();
					}
				if (flag) {
					List<Double6IntArrayPackage> d6iap = new LinkedList<Double6IntArrayPackage>();
					for (int i = 0; i < 9; i++)
						d6iap.add(new Double6IntArrayPackage(living.posX - 1 + living.rand.nextDouble() * 2, living.posY + 1,
								living.posZ - 1 + living.rand.nextDouble() * 2, 0D, 0D, 0D));
					AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.WATER_SPLASH, AABBHelper.getAABBFromEntity(living,
							AlchemyNetworkHandler.getParticleRange()), living.world, d6iap);
					living.getEntityData().setInteger(NBT_KEY_CD, living.ticksExisted);
					if (living instanceof EntityPlayerMP && !(living instanceof FakePlayer))
						AlchemyNetworkHandler.network_wrapper.sendTo(new MessagePurifyCallback(), (EntityPlayerMP) living);
				}
			}
		living.setAir(MAX_AIR);
	}
	
	@SubscribeEvent(priority = EventPriority.BOTTOM)
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		event.getEntity().getEntityData().removeTag(NBT_KEY_CD);
	}
	
	public static class MessagePurifyCallback implements IMessage, IMessageHandler<MessagePurifyCallback, IMessage> {
		
		@Override
		public void fromBytes(ByteBuf buf) { }

		@Override
		public void toBytes(ByteBuf buf) { }
		
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(MessagePurifyCallback message, MessageContext ctx) {
			AlchemyEventSystem.addDelayedRunnable(p -> type.restartCD(), 0);
			return null;
		}
		
	}
	
	@Override
	public Class<MessagePurifyCallback> getClientMessageClass() {
		return MessagePurifyCallback.class;
	}
	
	@Override
	public float modifyChangeRate(World world, EntityPlayer player, float changeRate, int trend) {
		return trend > 0 ? changeRate + TEMPERATURE_RATE : trend < 0 ? changeRate - TEMPERATURE_RATE : changeRate;
	}

	@Override
	public float modifyTarget(World world, EntityPlayer player, float temperature) {
		return temperature + TEMPERATURE_TARGET;
	}
	
	@Override
	public int getMaxCD() {
		return INTERVAL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().player;
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
		EntityPlayer player = Minecraft.getMinecraft().player;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 3;
	}

	public ItemAmuletPurify() {
		super("amulet_purify", 0x66CCFF);
	}

}
