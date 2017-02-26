package index.alchemy.core;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class AlchemyThrowables {
	
	private static final List<Throwable> throwables = Collections.synchronizedList(Lists.newLinkedList());
	
	public static List<Throwable> getThrowables() { return throwables; }

}
