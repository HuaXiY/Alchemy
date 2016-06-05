package index.alchemy.core.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;

public class AlchemyRuntimeExcption extends RuntimeException {

	public AlchemyRuntimeExcption(Exception e) {
		super(e);
		
		if (AlchemyModLoader.getProxy().isClient()) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String exceptionText = sw.toString();
			JDialog dialog = new JDialog();
			dialog.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(dialog, exceptionText,
					"Minecraft-" + Constants.MOD_ID, JOptionPane.ERROR_MESSAGE);
		}
	}
	
}