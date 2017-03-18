package index.alchemy.util;

import index.alchemy.core.AlchemyThreadManager;
import index.alchemy.core.debug.AlchemyRuntimeException;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import static index.alchemy.util.FunctionHelper.*;

import java.util.concurrent.CountDownLatch;

import org.jooq.lambda.fi.lang.CheckedRunnable;

public interface JFXHelper {
	
	static boolean isSupported() {
		return Platform.isSupported(ConditionalFeature.GRAPHICS) && Platform.isSupported(ConditionalFeature.CONTROLS);
	}
	
	static void runLater(Runnable runnable) {
		if (isSupported())
			AlchemyThreadManager.runOnNewThread(link(JFXPanel::new, () -> Platform.runLater(runnable)));
	}
	
	static void runAndWait(Runnable runnable) {
		if (isSupported()) {
			CountDownLatch latch = new CountDownLatch(1);
			AlchemyThreadManager.runOnNewThread(link(JFXPanel::new, () -> Platform.runLater(link(runnable, latch::countDown))));
			CheckedRunnable.unchecked(latch::await, AlchemyRuntimeException::onException).run();
		}
	}
	
}
