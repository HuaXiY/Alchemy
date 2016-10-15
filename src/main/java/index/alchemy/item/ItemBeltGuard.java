package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.entity.ai.EntityArrowTracker;
import index.alchemy.entity.ai.EntityHelper;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import index.alchemy.item.ItemBeltGuard.MessageGuardCallback;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double3Float2Package;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

public class ItemBeltGuard extends AlchemyItemBelt implements IEventHandle, INetworkMessage.Client<MessageGuardCallback>, ICoolDown {
	
	public static final int RECOVERY_INTERVAL = 20 * 6, RECOVERY_CD = 20 * 12, MAX_ABSORPTION = 20;
	public static final float DECREASE_COEFFICIENT = 0.01F;
	
	public boolean isCDOver(EntityLivingBase living) {
		return living.ticksExisted - living.getLastAttackerTime() > RECOVERY_CD
				&& living.ticksExisted - living.getCombatTracker().lastDamageTime > RECOVERY_CD;
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer())
			if (living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getAbsorptionAmount() < MAX_ABSORPTION && isCDOver(living))
				living.setAbsorptionAmount(min(living.getAbsorptionAmount() + 1, MAX_ABSORPTION));
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingAttackCallback(LivingAttackEvent event) {
		if (Always.isServer()) {
			EntityLivingBase living = event.getEntityLiving(), attacker = event.getSource().getEntity() instanceof EntityLivingBase ?
					(EntityLivingBase) event.getSource().getEntity() : null;
			if (isEquipmented(living)) {
				living.getCombatTracker().lastDamageTime = living.ticksExisted;
				if (living instanceof EntityPlayerMP)
					AlchemyNetworkHandler.network_wrapper.sendTo(new MessageGuardCallback(-1), (EntityPlayerMP) living);
			}
			if (attacker != null && isEquipmented(attacker)) {
				attacker.setLastAttacker(living);
				if (attacker instanceof EntityPlayerMP)
					AlchemyNetworkHandler.network_wrapper.sendTo(new MessageGuardCallback(living.getEntityId()), (EntityPlayerMP) attacker);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHurt(LivingHurtEvent event) {
		if (Always.isServer() && isEquipmented(event.getEntityLiving()) && event.getEntityLiving().getAbsorptionAmount() > 0F)
			event.setAmount(event.getAmount() * (1 - DECREASE_COEFFICIENT * event.getEntityLiving().getAbsorptionAmount()));
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
		if (message.lastAttackerId > -1) {
			World world = Minecraft.getMinecraft().theWorld;
			if (world != null) {
				Entity lastAttacker;
				if ((lastAttacker = world.getEntityByID(message.lastAttackerId)) != null)
					player.setLastAttacker(lastAttacker);
				else
					player.setLastAttacker(player);
			}
		} else
			player.getCombatTracker().lastDamageTime = player.ticksExisted;
		return null;
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

	public ItemBeltGuard() {
		super("belt_guard", 0xFFCC00);
	}

}