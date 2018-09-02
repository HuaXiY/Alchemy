package index.alchemy.core.asm.transformer;

import java.util.Map;

import index.alchemy.util.DeobfuscatingRemapper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

@Omega
public class SrgMap implements DeobfuscatingRemapper {

    public final Map<String, String>
            func = Maps.newHashMap(),
            field = Maps.newHashMap();

    public final BiMap<String, String>
            clazz = HashBiMap.create(),
            inverse_clazz;

    public SrgMap(String srg_mcp) {
        String[] maps = srg_mcp.split("\n");
        for (String str : maps) {
            if (str.length() < 3)
                continue;
            String sa[] = str.split(" "), src, to;
            if (str.startsWith("CL:")) {
                src = sa[1];
                to = sa[2];
                if (!src.equals(to))
                    clazz.put(src, to);
            }
            else if (str.startsWith("MD:")) {
                src = Tool.get(sa[1], ".*/(.*)");
                to = Tool.get(sa[sa.length - 2], ".*/(.*)");
                if (!src.equals(to))
                    func.put(src, to);
            }
            else if (str.startsWith("FD:")) {
                src = Tool.get(sa[1], ".*/(.*)");
                to = Tool.get(sa[2], ".*/(.*)");
                if (!src.equals(to))
                    field.put(src, to);
            }
        }
        inverse_clazz = clazz.inverse();
    }

    @Override
    public String unmapType(String typeName) {
        return Tool.isNullOr(inverse_clazz.get(typeName), typeName);
    }

    @Override
    public String mapType(String typeName) {
        return Tool.isNullOr(clazz.get(typeName), typeName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        return Tool.isNullOr(func.get(name), name);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        return Tool.isNullOr(field.get(name), name);
    }

}
