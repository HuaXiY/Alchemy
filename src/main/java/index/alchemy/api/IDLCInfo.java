package index.alchemy.api;

import java.io.File;
import java.util.List;

import index.alchemy.api.annotation.DLC;
import index.alchemy.core.AlchemyDLCLoader.DLCContainer;

public interface IDLCInfo extends DLC {
	
	Class<?> getDLCMainClass();
	
	DLCContainer getDLCContainer();
	
	List<String> getDLCAllClass();
	
	File getDLCFile();

}
