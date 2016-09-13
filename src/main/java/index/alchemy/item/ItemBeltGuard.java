package index.alchemy.item;

import index.alchemy.api.Always;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.entity.ai.EntityArrowTracker;
import index.alchemy.entity.ai.EntityHelper;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import index.alchemy.item.ItemBeltGuard.MessageGuardCallback;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double3Float2Package;
import index.alchemy.util.AABBHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

import java.util.LinkedList;
import java.util.List;

public class ItemBeltGuard extends AlchemyItemBelt implements IEventHandle, INetworkMessage.Client<MessageGuardCallback>, ICoolDown {
	
	public static final int RECOVERY_INTERVAL = 20 * 3, RECOVERY_CD = 20 * 8, MAX_ABSORPTION = 20;
	public static final float DECREASE = 0.8F;
	
	public boolean isCDOver(EntityLivingBase living) {
		return living.ticksExisted - living.getLastAttackerTime() > RECOVERY_CD
				&& living.ticksExisted - living.getCombatTracker().lastDamageTime > RECOVERY_CD;
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer()) {
			if (living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getAbsorptionAmount() < MAX_ABSORPTION && isCDOver(living))
				living.setAbsorptionAmount(min(living.getAbsorptionAmount() + 1, MAX_ABSORPTION));
		} else if (living == Minecraft.getMinecraft().thePlayer && living.lastDamage != -1) {
			living.lastDamage = -1;
			living.getCombatTracker().lastDamageTime = living.ticksExisted;
		}
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		if (Always.isServer() && event.getSource().getSourceOfDamage() instanceof EntityArrow
				&& isEquipmented(event.getEntityLiving()) && isCDOver(event.getEntityLiving())) {
			event.setCanceled(true);
			EntityLivingBase living = event.getEntityLiving();
			EntityArrow arrow = (EntityArrow) event.getSource().getSourceOfDamage();
			if (arrow.shootingEntity != null && arrow.shootingEntity != event.getEntityLiving()) {
				EntityArrow reflect = EntityHelper.respawnArrow(arrow);
				EntityArrowTracker.track(reflect, arrow.shootingEntity);
				reflect.setDamage(16);
				reflect.shootingEntity = living;
				arrow.worldObj.spawnEntityInWorld(reflect);
				living.setLastAttacker(arrow.shootingEntity);
				Double3Float2Package d3f2p = new Double3Float2Package(living.posX, living.posY, living.posZ, 1F,
						1F / (living.rand.nextFloat() * 0.4F + 0.8F));
				List<Double3Float2Package> d3f2ps = new LinkedList<Double3Float2Package>();
				d3f2ps.add(d3f2p);
				AlchemyNetworkHandler.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.PLAYERS,
						AABBHelper.getAABBFromEntity(living, AlchemyNetworkHandler.getSoundRange()), living.worldObj, d3f2ps);
				if (living instanceof EntityPlayerMP)
					AlchemyNetworkHandler.network_wrapper.sendTo(new MessageGuardCallback(arrow.shootingEntity.getEntityId()),
							(EntityPlayerMP) living);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHurt(LivingHurtEvent event) {
		if (Always.isServer() && isEquipmented(event.getEntityLiving()) && event.getEntityLiving().getAbsorptionAmount() > 0F)
			event.setAmount(event.getAmount() * DECREASE);
	}
	
	public static class MessageGuardCallback implements IMessage {
		
		public int lastAttackerId = -1;
		
		public MessageGuardCallback() { }
		
		public MessageGuardCallback(int lastAttackerId) {
			this.lastAttackerId = lastAttackerId;
		}
		
		@Override
		public void fromBytes(ByteBuf buf) {
			lastAttackerId = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(lastAttackerId);
		}
	}
	
	@Override
	public Class<MessageGuardCallback> getClientMessageClass() {
		return MessageGuardCallback.class;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageGuardCallback message, MessageContext ctx) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (message.lastAttackerId != -1) {
			World world = Minecraft.getMinecraft().theWorld;
			if (world != null) {
				Entity lastAttacker;
				if ((lastAttacker = world.getEntityByID(message.lastAttackerId)) != null)
					player.setLastAttacker(lastAttacker);
				else
					player.setLastAttacker(player);
			}
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientAttackEntity(AttackEntityEvent event) {
		if (Always.isClient() && event.getEntityLiving() == Minecraft.getMinecraft().thePlayer)
			event.getEntityLiving().setLastAttacker(event.getTarget());
	}
	
	@Override
	public int getMaxCD() {
		return RECOVERY_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return isEquipmented(player) ? 
				max(0, getMaxCD() - (player.ticksExisted - max(player.getLastAttackerTime(), player.getCombatTracker().lastDamageTime))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderCD(int x, int y, int w, int h) {}

	public ItemBeltGuard() {
		super("belt_guard", 0xFFCC00);
	}

}