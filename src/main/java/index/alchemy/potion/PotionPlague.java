package index.alchemy.potion;

import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.EventType;
import index.alchemy.entity.AlchemyDamageSourceLoader;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PotionPlague extends AlchemyPotion implements IEventHandle {
	
	@Override
	public boolean isReady(int tick, int level) {
		return tick == 256 >> level;
	}
	
	@Override
	public void performEffect(EntityLivingBase living, int level) {
		living.attackEntityFrom(AlchemyDamageSourceLoader.plague, 1F);
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		EntityLivingBase living = event.getEntityLiving(), spawn_living = null;
		if (living instanceof EntityPlayer || living instanceof EntityVillager) {
			spawn_living = new EntityZombie(living.worldObj);
			((EntityZombie) spawn_living).setChild(living.isChild());
			if (living instanceof EntityVillager)
				((EntityZombie) spawn_living).setVillagerType(RANDOM.nextInt(5));
		} else if (living instanceof EntityPig) {
			spawn_living = new EntityPigZombie(living.worldObj);
		}
		
		if (spawn_living != null) {
			if (spawn_living.hasCustomName()) {
				spawn_living.setCustomNameTag(living.getCustomNameTag());
				spawn_living.setAlwaysRenderNameTag(living.getAlwaysRenderNameTag());
            }
			IItemHandler item = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), 
					spawn_item = spawn_living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0, len = item.getSlots(); i < len; i++)
				spawn_item.insertItem(i, item.getStackInSlot(i).copy(), true);
			spawn_living.copyLocationAndAnglesFrom(living);
			living.worldObj.spawnEntityInWorld(spawn_living);
		}
	}
	
	public PotionPlague() {
		super("plague", true, 0x000000);
	}

}