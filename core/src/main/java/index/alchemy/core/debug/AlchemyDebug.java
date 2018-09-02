package index.alchemy.core.debug;

import java.util.Map;

import index.project.version.annotation.Omega;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Omega
public class AlchemyDebug {

//	@Remote.Provider
//	public static class RemoteSupport {
//		
//		@SideOnly(Side.CLIENT)
//		public static void handleClientThrowable(Throwable throwable) {
//			if (NetworkHelper.isRemoteGaming())
//				uploadThrowable(Stream.of(Minecraft.getMinecraft().player), throwable);
//		}
//		
//		@Remote(Side.SERVER)
//		public static void uploadThrowable(Stream<EntityPlayer> players, Throwable throwable) {
//			//
//		}
//		
//	}
    
    public static final Logger logger = LogManager.getLogger(AlchemyDebug.class.getSimpleName());
    
    private static final Map<String, Long> time_mapping = Maps.newHashMap();
    
    public static final void start(String name) {
        time_mapping.put(name, System.currentTimeMillis());
        logger.info(name + ": begin");
    }
    
    public static final void end(String name) {
        long time = System.currentTimeMillis() - time_mapping.get(name);
        logger.info(name + ": done, " + (time >= 1000 ? time / 1000 + "s" : time + "ms"));
    }
    
    public static final void println(Object message) {
        logger.info(message);
    }
    
}