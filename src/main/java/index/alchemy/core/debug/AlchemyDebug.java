package index.alchemy.core.debug;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import index.project.version.annotation.Omega;

@Omega
public class AlchemyDebug {

	public static final Logger logger = LogManager.getLogger(AlchemyDebug.class.getSimpleName());
	
	private static final Map<String, Long> time_mapping = new HashMap<String, Long>();
	
	public static final void start(String name) {
		time_mapping.put(name, System.currentTimeMillis());
		logger.info(name + ": begin");
	}
	
	public static final void end(String name) {
		long time = System.currentTimeMillis() - time_mapping.get(name);
		logger.info(name + ": done, " + (time >= 1000 ? time / 1000 + "s" : time + "ms"));
	}
}