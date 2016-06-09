package index.alchemy.core.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

import index.alchemy.annotation.Config;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.Tool;
import net.minecraftforge.fml.client.SplashProgress;

public class AlchemyRuntimeExcption extends RuntimeException {
	
	@Config(category = "runtime", comment = "Serious exceptions are ignored in the game.")
	private static boolean ignore_serious_exceptions = false;

	private AlchemyRuntimeExcption(Throwable t) {
		super(t);
	}
	
	public static void onExcption(Throwable t) {
		AlchemyModLoader.logger.error(t);
		
		if (ignore_serious_exceptions)
			return;
		
		AlchemyRuntimeExcption e = new AlchemyRuntimeExcption(t);
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		if (AlchemyModLoader.getProxy().isClient()) {
			Tool.set(SplashProgress.class, 4, e);
			SplashProgress.finish();
			
	        // TODO
		}
		
	}
	
}