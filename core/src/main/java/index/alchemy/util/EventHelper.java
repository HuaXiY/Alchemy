package index.alchemy.util;

import index.project.version.annotation.Omega;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

@Omega
public interface EventHelper {
	
	static IEventListener[] getAllHandler(Class<? extends Event> clazz, int busId) {
		ListenerList list = ReflectionHelper.allocateInstance(clazz).getListenerList();
		return list == null ? new IEventListener[0] : list.lists[busId].getListeners();
	}
	
	static void unregister(EventBus bus, Class<?> hanlder) {
		bus.listenerOwners.keySet().stream().filter(hanlder::isInstance).forEach(bus::unregister);
	}

}
