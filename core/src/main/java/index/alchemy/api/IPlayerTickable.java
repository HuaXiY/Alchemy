package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public interface IPlayerTickable {
    
    @Nullable
    default Side getSide() { return null; }
    
    void onTick(EntityPlayer player, Phase phase);
    
}
