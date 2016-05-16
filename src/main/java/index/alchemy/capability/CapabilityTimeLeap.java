package index.alchemy.capability;

import java.util.LinkedList;

import index.alchemy.annotation.InitInstance;
import index.alchemy.api.IEventHandle;
import index.alchemy.capability.CapabilityTimeLeap.TimeSnapshot;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityTimeLeap extends AlchemyCapability<TimeSnapshot> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("time_leap");
	
	public static class TimeSnapshot implements ICapabilityProvider {
		
		public static class TimeNode {
			
			public final double x, y, z;
			public final float yaw, pitch;
			public final float health, absorption;
			public final int food, air;
			
			public TimeNode(EntityPlayer player) {
				x = player.posX;
				y = player.posY;
				z = player.posZ;
				yaw = player.rotationYaw;
				pitch = player.rotationPitch;
				health = player.getHealth();
				absorption = player.getAbsorptionAmount();
				food = player.getFoodStats().getFoodLevel();
				air = player.getAir();
			}
			
			@SideOnly(Side.CLIENT)
			public void updatePlayerOnClient(EntityPlayer player) {
				player.setPosition(x, y, z);
				player.rotationYaw = yaw;
				player.rotationPitch = pitch;
				player.motionX = 0;
				player.motionY = 0;
				player.motionZ = 0;
			}
			
			public void updatePlayerOnServer(EntityPlayer player) {
				player.setHealth(health);
				player.setAbsorptionAmount(absorption);
				player.getFoodStats().setFoodLevel(food);
				player.setAir(air);
				player.motionX = 0;
				player.motionY = 0;
				player.motionZ = 0;
				player.fallDistance = 0;
			}
			
		}
		
		public static final int SIZE = 60;
		
		public final LinkedList<TimeNode> list = new LinkedList<TimeNode>();
		
		private boolean update = true;
		
		public boolean isUpdate() {
			return update;
		}
		
		public void setUpdate(boolean update) {
			this.update = update;
		}
		
		public void updateTimeNode(EntityPlayer player) {
			if (update) {
				if (list.size() >= SIZE)
					list.removeLast();
				list.addFirst(new TimeNode(player));
			}
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return AlchemyCapabilityLoader.time_leap == capability;
		}
		
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (hasCapability(capability, facing))
				return (T) this;
			return null;
		}
		
	}
	
	@Override
	public Class<TimeSnapshot> getDataClass() {
		return TimeSnapshot.class;
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent
	public void onAttachCapabilities_Entity(AttachCapabilitiesEvent.Entity event) {
		if (event.getEntity() instanceof EntityPlayer) {
			event.addCapability(RESOURCE, new TimeSnapshot());
			AlchemyItemLoader.ring_time.onCapabilityInit((EntityPlayer) event.getEntity());
		}
	}

}