package index.alchemy.api;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public interface IPhaseRunnable {
	
	public void run(Phase phase);

}
