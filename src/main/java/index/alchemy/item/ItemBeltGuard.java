package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBeltGuard extends AlchemyItemBelt implements IEventHandle, ICoolDown {
	
	public static final int RECOVERY_INTERVAL = 20 * 3, RECOVERY_CD = 20 * 6, MAX_ABSORPTION = 20;
	public static final float DECREASE = 0.8F;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Alway.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getAbsorptionAmount() < MAX_ABSORPTION
				&& living.ticksExisted - living.getLastAttackerTime() > RECOVERY_CD
				&& living.ticksExisted - living.getCombatTracker().lastDamageTime > RECOVERY_CD)
			living.setAbsorptionAmount(Math.min(living.getAbsorptionAmount() + 1, MAX_ABSORPTION));
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLivingHurt(LivingHurtEvent event) {
		if (isEquipmented(event.getEntityLiving()) && event.getEntityLiving().getAbsorptionAmount() > 0F)
			event.setAmount(event.getAmount() * DECREASE);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientLivingHurt(LivingHurtEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (event.getEntityLiving() == player)
			player.getCombatTracker().lastDamageTime = player.ticksExisted;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onClientLivingAttack(AttackEntityEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (event.getEntityLiving() == player)
			player.setLastAttacker(event.getTarget());
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
				Math.max(0, getMaxCD() - (player.ticksExisted - Math.max(player.getLastAttackerTime(), player.getCombatTracker().lastDamageTime))) : -1;
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