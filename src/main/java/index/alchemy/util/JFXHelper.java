package index.alchemy.util;

import index.alchemy.core.AlchemyThreadManager;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import static index.alchemy.util.FunctionHelper.*;

import java.util.concurrent.CountDownLatch;

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
			try { latch.await(); } catch (InterruptedException e) { }
		}
	}
	
}
