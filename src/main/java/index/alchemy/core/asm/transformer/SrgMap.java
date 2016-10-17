package index.alchemy.core.asm.transformer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import index.alchemy.util.Tool;

public class SrgMap {
	
	public final Map<String, String>
			func = new HashMap<String, String>(),
			field = new HashMap<String, String>(),
			clazz = new HashMap<String, String>();
	
	public static SrgMap get(File srg_mcp) throws IOException { 
		SrgMap srg = new SrgMap();
		String[] maps = Tool.read(srg_mcp).split("\n");
		for (int index = 0, len = maps.length; index < len; index++) {
			String str = maps[index];
			if (str.length() < 3) 
				continue;
			String sa[] = str.split(" "), src, to;
			if (str.startsWith("CL:")) {
				src = sa[1];
				to = sa[2];
				if (!src.equals(to)) 
					srg.clazz.put(src, to);
			} else if (str.startsWith("MD:")) {
				src = Tool.get(sa[1], ".*/(.*)");
				to = Tool.get(sa[sa.length - 2], ".*/(.*)");
				if (!src.equals(to)) 
					srg.func.put(src, to);
			} else if (str.startsWith("FD:")) {
				src = Tool.get(sa[1], ".*/(.*)");
				to = Tool.get(sa[2], ".*/(.*)");
				if (!src.equals(to)) 
					srg.field.put(src, to);
			}
		}
		return srg;
	}
	
}