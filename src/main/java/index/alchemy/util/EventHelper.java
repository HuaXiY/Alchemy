package index.alchemy.util;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

public class EventHelper {
	
	public static final IEventListener[] getAllHandler(Class<? extends Event> clazz, int busId) {
		ListenerList list = ReflectionHelper.allocateInstance(clazz).getListenerList();
		return list == null ? new IEventListener[0] : list.lists[busId].getListeners();
	}
	
	public static final void unregister(EventBus bus, Class<?> hanlder) {
		bus.listenerOwners.keySet().stream().filter(hanlder::isInstance).forEach(bus::unregister);
	}

}
