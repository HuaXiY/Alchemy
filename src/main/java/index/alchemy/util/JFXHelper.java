package index.alchemy.util;

import index.alchemy.core.AlchemyThreadManager;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import static index.alchemy.util.FunctionHelper.*;

public class JFXHelper {
	
	public static void runLater(Runnable runnable) {
		if (Platform.isSupported(ConditionalFeature.GRAPHICS) && Platform.isSupported(ConditionalFeature.CONTROLS))
			AlchemyThreadManager.runOnNewThread(link(JFXPanel::new, () -> Platform.runLater(runnable)));
	}

}
