package index.alchemy.api;

import java.net.URL;
import java.util.List;

import javax.annotation.PropertyKey;

import index.alchemy.api.annotation.DLC;
import index.alchemy.core.AlchemyDLCLoader.DLCContainer;
import index.alchemy.util.Pointer;

@PropertyKey
public interface IDLCInfo extends DLC {
	
	Class<?> getDLCMainClass();
	
	void clinitDLCMainClass();
	
	DLCContainer getDLCContainer();
	
	List<String> getDLCAllClass();
	
	URL getDLCURL();
	
	Pointer<Boolean> state();
	
	default boolean shouldLoad() {
		return state().getValue();
	}
	
	default boolean shouldInjectLoader() {
		return shouldLoad();
	}
	
	default String getInfo() {
		return "DLC " + name() + "|" + id() + ", version: " + version() + ", forgeVersion: " + forgeVersion() + ", description: " + description();
	}
	
}
