package index.alchemy.api;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;

public interface IEventHandle {
	
	default EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
}