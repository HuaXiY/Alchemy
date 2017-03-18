package index.alchemy.api;

import java.io.File;
import java.util.List;

import javax.annotation.PropertyKey;

import index.alchemy.api.annotation.DLC;
import index.alchemy.core.AlchemyDLCLoader.DLCContainer;

@PropertyKey
public interface IDLCInfo extends DLC {
	
	Class<?> getDLCMainClass();
	
	void clinitDLCMainClass();
	
	DLCContainer getDLCContainer();
	
	List<String> getDLCAllClass();
	
	File getDLCFile();

}
