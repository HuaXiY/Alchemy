package index.alchemy.api;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public interface IContinuedRunnable {
	
	boolean run(Phase phase);

}