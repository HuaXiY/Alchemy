package index.alchemy.core;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import index.alchemy.core.debug.JFXDialog;

public class AlchemyThrowables {
	
	private static final List<Throwable> throwables = Collections.synchronizedList(Lists.newLinkedList());
	
	public static List<Throwable> throwables() { return throwables; }
	
	public static synchronized void checkThrowables() {
		throwables().forEach(JFXDialog::showThrowableAndWait);
		throwables().clear();
	}

}
