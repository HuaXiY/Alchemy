package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public interface IPlayerTickable {
	
	@Nullable
	Side getSide();
	
	void onTick(EntityPlayer player, Phase phase);
	
}
