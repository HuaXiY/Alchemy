package index.alchemy.api;

import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;

public interface IEventHandle {
	
	default EventType[] getEventTypes() { return AlchemyEventSystem.EVENT_BUS; }
	
	interface Terrain extends IEventHandle {
		
		default EventType[] getEventTypes() { return AlchemyEventSystem.TERRAIN_GEN_BUS; }
		
	}
	
	interface Ore extends IEventHandle {
		
		default EventType[] getEventTypes() { return AlchemyEventSystem.ORE_GEN_BUS; }
		
	}
	
}