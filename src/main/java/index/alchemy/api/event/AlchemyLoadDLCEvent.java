package index.alchemy.api.event;

import java.io.File;

import index.alchemy.api.annotation.DLC;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AlchemyLoadDLCEvent extends Event {
	
	public final DLC dlc;
	public final File file;
	
	public AlchemyLoadDLCEvent(DLC dlc, File file) {
		this.dlc = dlc;
		this.file = file;
	}
	
	@Cancelable
	public static class Pre extends AlchemyLoadDLCEvent {

		public Pre(DLC dlc, File file) {
			super(dlc, file);
		}
		
	}
	
	public static class Post extends AlchemyLoadDLCEvent {

		public Post(DLC dlc, File file) {
			super(dlc, file);
		}
		
	}

}