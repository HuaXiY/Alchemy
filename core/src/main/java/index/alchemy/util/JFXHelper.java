package index.alchemy.util;

import java.util.concurrent.CountDownLatch;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;

import static index.alchemy.util.FunctionHelper.*;

@Omega
public interface JFXHelper {
	
	static void setImplicitExit(boolean flag) {
		Platform.setImplicitExit(flag);
	}
	
	static boolean isSupported() {
		return Platform.isSupported(ConditionalFeature.GRAPHICS) && Platform.isSupported(ConditionalFeature.CONTROLS);
	}
	
	static void startup( ) {
		try { Platform.startup(() -> {}); }
		catch (IllegalStateException e) { }
	}
	
	static void runLater(Runnable runnable) {
		if (isSupported()) {
			startup();
			Platform.runLater(runnable);
		}
	}
	
	static void runAndWait(Runnable runnable) {
		if (isSupported()) {
			startup();
			CountDownLatch latch = new CountDownLatch(1);
			Platform.runLater(link(runnable, latch::countDown));
			FunctionHelper.onThrowableRunnable(latch::await, AlchemyRuntimeException::onException).run();
		}
	}
	
}
