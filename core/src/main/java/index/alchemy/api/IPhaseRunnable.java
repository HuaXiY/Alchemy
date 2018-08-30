package index.alchemy.api;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@FunctionalInterface
public interface IPhaseRunnable {
	
	void run(Phase phase);

}
