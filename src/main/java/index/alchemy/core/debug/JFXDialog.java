package index.alchemy.core.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

import index.alchemy.util.JFXHelper;
import index.project.version.annotation.Alpha;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

@Alpha
public class JFXDialog {
	
	public static void showThrowable(Throwable ex) { showThrowable(ex, null, null); }
	
	public static void showThrowableAndWait(Throwable ex, String title, String str) {
		JFXHelper.runAndWait(getShowAlertRunnable(ex, title, str));
	}
	
	public static void showThrowable(Throwable ex, String title, String str) {
		
		JFXHelper.runLater(getShowAlertRunnable(ex, title, str));
	}
	
	public static Runnable getShowAlertRunnable(Throwable ex, String title, String str) {
		return () -> {
			Alert alert = new Alert(AlertType.ERROR);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.setAlwaysOnTop(true);
			alert.initOwner(null);
			alert.setTitle(title == null ? "Exception Dialog" : title);
			alert.setHeaderText(str == null ?
					"The program has an unknown exception, please send a message to 931920447@qq.com for help." : str);
			alert.setContentText(ex.getClass().getName() + ": " + ex.getMessage());
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			String exceptionText = sw.toString();
			Label label = new Label("The exception stacktrace was: ");
			TextArea textArea = new TextArea(exceptionText);
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);
			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);
			alert.getDialogPane().setExpandableContent(expContent);
			alert.show();
		};
	}
	
}
