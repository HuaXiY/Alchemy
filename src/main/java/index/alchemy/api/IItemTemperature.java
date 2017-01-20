package index.alchemy.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IItemTemperature {
	
	float modifyChangeRate(World world, EntityPlayer player, float changeRate, int trend);
	
	float modifyTarget(World world, EntityPlayer player, float temperature);

}
