package index.alchemy.api;

import java.io.File;
import java.util.List;

import index.alchemy.api.annotation.DLC;
import net.minecraftforge.fml.common.DummyModContainer;

public interface IDLCInfo extends DLC {
	
	Class<?> getDLCMainClass();
	
	DummyModContainer getDLCContainer();
	
	List<String> getDLCAllClass();
	
	File getDLCFile();

}
