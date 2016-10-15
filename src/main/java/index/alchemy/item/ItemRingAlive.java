package index.alchemy.item;

import index.alchemy.api.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.util.AABBHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemRingAlive extends AlchemyItemRing implements IEventHandle {
	
	public static final int RADIUS = 12;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (living.getCreatureAttribute() != EnumCreatureAttribute.UNDEAD)
			for (EntityPlayer player : living.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AABBHelper.getAABBFromEntity(living, RADIUS)))
				if (isEquipmented(player))
					player.heal(living.getMaxHealth() / 10);
	}

	public ItemRingAlive() {
		super("ring_alive", 0x7E23D4);
	}

}