package index.alchemy.api;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public interface IIndexRunnable {
	
	void run(int index, Phase phase);

}
