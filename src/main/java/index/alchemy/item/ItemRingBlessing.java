package index.alchemy.item;

import index.alchemy.api.IEventHandle;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Alpha
public class ItemRingBlessing extends AlchemyItemRing implements IEventHandle {
	
	public static final String DAMAGETYPE = "player";
	public static final float HURT_MIN_VALUE = 3, HURT_PERCENTAGE = 0.02F;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLivingHurt(LivingHurtEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		Entity source = event.getSource().getSourceOfDamage();
		if (source != null && source instanceof EntityPlayer &&
				event.getSource().getDamageType().equals(DAMAGETYPE) && isEquipmented((EntityPlayer) source))
			event.setAmount(event.getAmount() + Math.max(event.getEntityLiving().getMaxHealth() * HURT_PERCENTAGE, HURT_MIN_VALUE));
	}

	public ItemRingBlessing() {
		super("ring_blessing", 0x6ACCA9);
	}

}