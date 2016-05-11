package index.alchemy.item;

import java.lang.reflect.Field;

import index.alchemy.api.Alway;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import index.alchemy.util.Tool;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatTracker;

public class ItemBeltGuard extends AlchemyItemBelt {
	
	public static final int RECOVERY_INTERVAL = 20 * 3, RECOVERY_CD = 20 * 6, MAX_ABSORPTION = 20;
	
	private static Field lastDamageTime = Tool.setAccessible(CombatTracker.class.getDeclaredFields()[2]);
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		try {
			if (Alway.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getAbsorptionAmount() < MAX_ABSORPTION
					&& living.ticksExisted - living.getLastAttackerTime() > RECOVERY_CD
					&& living.ticksExisted - ((Integer) lastDamageTime.get(living.getCombatTracker())) > RECOVERY_CD)
				living.setAbsorptionAmount(living.getAbsorptionAmount() + 1);
		} catch (Exception e) {
			throw new AlchemyRuntimeExcption(e);
		}
	}

	public ItemBeltGuard() {
		super("belt_guard", 0xFFCC00);
	}

}