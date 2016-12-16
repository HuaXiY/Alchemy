package index.alchemy.config;

import org.apache.commons.lang3.ArrayUtils;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Config.Handle.Type;
import index.project.version.annotation.Omega;

@Omega
public class AlchemyConfig {
	
	public static final String HANDLE_INT_ARRAY = "int_array";
	
	@Config.Handle(name = HANDLE_INT_ARRAY, type = Type.MAKE)
	public static int[] makeIntArray(String[] sa) {
		int ia[] = new int[sa.length];
		int index = 0;
		for (String s : sa)
			try {
				ia[index] = Integer.valueOf(s);
				index++;
			} catch (Exception e) { }
		if (index != sa.length)
			ia = ArrayUtils.subarray(ia, 0, index);
		return ia;
	}
	
	@Config.Handle(name = HANDLE_INT_ARRAY, type = Type.SAVE)
	public static String[] saveStringArray(int[] ia) {
		String sa[] = new String[ia.length];
		int index = 0;
		for (int i : ia)
			sa[index++] = String.valueOf(i);
		return sa;
	}

}