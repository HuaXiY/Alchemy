package index.alchemy.capability;

import java.util.Iterator;
import java.util.LinkedList;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

@Omega
@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityTimeLeap extends AlchemyCapability<TimeSnapshot> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("time_leap");
	
	public static class TimeSnapshot implements ICapabilityProvider {
		
		public static class TimeNode {
			
			public final double x, y, z;
			public final float yaw, pitch;
			public final float health, absorption;
			public final int food, air, fire;
			public final TimeNode riding;
			
			public TimeNode(Entity entity) {
				x = entity.posX;
				y = entity.posY;
				z = entity.posZ;
				yaw = entity.rotationYaw;
				pitch = entity.rotationPitch;
				air = entity.getAir();
				fire = entity.fire;
				if (entity instanceof EntityLivingBase) {
					EntityLivingBase living = (EntityLivingBase) entity;
					health = living.getHealth();
					absorption = living.getAbsorptionAmount();
					if (living instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) living;
						food = player.getFoodStats().getFoodLevel();
					} else
						food = 0;
				} else
					health = absorption = food = 0;
				riding = entity.getRidingEntity() != null ? new TimeNode(entity.getRidingEntity()) : null;
			}
			
			@SideOnly(Side.CLIENT)
			public void updateEntityOnClient(Entity entity) {
				if (Always.calculateTheStraightLineDistance(entity.posX - x, entity.posY - y, entity.posZ - z) < 900) {
					entity.prevPosX = entity.posX;
					entity.prevPosY = entity.posY;
					entity.prevPosZ = entity.posZ;
					entity.setPosition(x, y, z);
				}
				entity.prevRotationYaw = entity.rotationYaw;
				entity.prevRotationPitch = entity.rotationPitch;
				entity.rotationYaw = yaw;
				entity.rotationPitch = pitch;
				entity.fire = fire;
				if (entity.getRidingEntity() != null)
					if (riding != null)
						riding.updateEntityOnClient(entity.getRidingEntity());
					else
						entity.dismountRidingEntity();
			}
			
			public void updateEntityOnServer(Entity entity) {
				entity.setAir(air);
				entity.fire = fire;
				entity.fallDistance = 0;
				if (entity instanceof EntityLivingBase) {
					EntityLivingBase living = (EntityLivingBase) entity;
					living.setHealth(health);
					living.setAbsorptionAmount(absorption);
					if (living instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) living;
						player.getFoodStats().setFoodLevel(food);
						if (Always.calculateTheStraightLineDistance(player.posX - x, player.posY - y, player.posZ - z) > 900)
							player.setPositionAndUpdate(x, y, z);
					}
				}
				if (entity.getRidingEntity() != null)
					if (riding != null)
						riding.updateEntityOnServer(entity.getRidingEntity());
					else
						entity.dismountRidingEntity();
			}
			
		}
		
		public static final int SIZE = 80;
		
		public final LinkedList<TimeNode> list = new LinkedList<TimeNode>();
		private boolean update = true, leaping;
		
		public void clear() {
			list.clear();
		}
		
		public Iterator<TimeNode> iterator() {
			return list.iterator();
		}
		
		public void setLeaping(boolean leaping) {
			this.leaping = leaping;
		}
		
		public boolean isLeaping() {
			return leaping;
		}
		
		public boolean isUpdate() {
			return update;
		}
		
		public void setUpdate(boolean update) {
			this.update = update;
		}
		
		public void updateTimeNode(Entity entity) {
			if (update) {
				if (list.size() >= SIZE)
					list.removeLast();
				list.addFirst(new TimeNode(entity));
			}
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return AlchemyCapabilityLoader.time_leap == capability;
		}
		
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? (T) this : null;
		}
		
	}
	
	@Override
	public Class<TimeSnapshot> getDataClass() {
		return TimeSnapshot.class;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onEntityViewRender_FOVModifier(EntityViewRenderEvent.FOVModifier event) {
		Entity render = Minecraft.getMinecraft().getRenderViewEntity();
		if (render != null) {
			TimeSnapshot snapshot = render.getCapability(AlchemyCapabilityLoader.time_leap, null);
			if (snapshot != null && snapshot.isLeaping())
				event.setFOV(max(80, event.getFOV()));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		TimeSnapshot snapshot = event.getEntityLiving().getCapability(AlchemyCapabilityLoader.time_leap, null);
		if (snapshot != null && snapshot.isLeaping())
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	@SubscribeEvent
	public void onAttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
		event.addCapability(RESOURCE, new TimeSnapshot());
	}

}