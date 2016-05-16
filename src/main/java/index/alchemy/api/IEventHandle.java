package index.alchemy.api;

import index.alchemy.core.AlchemyEventSystem.EventType;

public interface IEventHandle {
	
	public EventType[] getEventType();
	
}