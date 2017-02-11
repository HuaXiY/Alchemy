package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.Patch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

@Patch("net.minecraft.util.FoodStats")
public class ExFoodState extends FoodStats {
	
	@Override
	public void onUpdate(EntityPlayer player) {
		EnumDifficulty enumdifficulty = player.worldObj.getDifficulty();
		prevFoodLevel = foodLevel;
		
		ThirstHandler thirst = (ThirstHandler) ThirstHelper.getThirstData(player);
		if (player.ticksExisted % 20 == 0) {
			addExhaustion(0.02F);
			thirst.addExhaustion(0.04F);
		}
		
		if (foodExhaustionLevel > 4.0F) {
			foodExhaustionLevel -= 4.0F;
			if (foodSaturationLevel > 0.0F)
				foodSaturationLevel = Math.max(foodSaturationLevel - 1.0F, 0.0F);
			else if (enumdifficulty != EnumDifficulty.PEACEFUL)
				foodLevel = Math.max(foodLevel - 1, 0);
		}
		boolean flag = player.worldObj.getGameRules().getBoolean("naturalRegeneration");

		if (flag && foodSaturationLevel > 0 && player.shouldHeal() && thirst.getThirst() > 3) {
			if (prevFoodLevel <= 0)
				foodTimer = 0;
			++foodTimer;
			if (foodTimer >= 120) {
				player.heal(1.0F);
				addExhaustion(5.0F);
				foodTimer = 0;
			}
		} else if (foodLevel <= 0) {
			if (prevFoodLevel > 0)
				foodTimer = 0;
			++foodTimer;
			if (foodTimer >= 80) {
				if (player.getHealth() > player.getMaxHealth() / 2 || enumdifficulty == EnumDifficulty.HARD || player.getHealth() > 1.0F &&
						enumdifficulty == EnumDifficulty.NORMAL)
					player.attackEntityFrom(DamageSource.starve, 1.0F);
				foodTimer = 0;
			}
		}
	}

}
