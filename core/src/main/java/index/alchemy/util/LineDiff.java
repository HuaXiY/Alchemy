package index.alchemy.util;

import java.util.LinkedList;

import index.alchemy.util.LineDiff.Diff.Type;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

public interface LineDiff {
    
    class Diff {
        
        public enum Type {
            DELETE, INSERT, EQUAL
        }
        
        public final Type type;
        public final String line;
        
        public Diff(Type type, String line) {
            this.type = type;
            this.line = line;
        }
        
    }
    
    static LinkedList<Diff> diff(String source[], String target[]) {
        LinkedList<Diff> resule = Lists.newLinkedList();
        for (int i = 0, offset = -1; i < source.length; i++) {
            String line = source[i];
            int index = ArrayUtils.indexOf(target, line, offset);
            if (index == -1)
                resule.add(new Diff(Type.DELETE, line));
            else if (index == offset + 1)
                resule.add(new Diff(Type.EQUAL, line));
            else {
                for (int k = offset + 1; k < index; k++)
                    resule.add(new Diff(Type.INSERT, target[k]));
                resule.add(new Diff(Type.EQUAL, target[index]));
            }
            offset = Math.max(offset, index);
        }
        return resule;
    }
    
    static String comparing(String source, String target, boolean eq) {
        LinkedList<Diff> diffs = diff(source.split("\n"), target.split("\n"));
        StringBuilder builder = new StringBuilder(Math.max(source.length(), target.length()) * 2);
        for (Diff diff : diffs) {
            switch (diff.type) {
                case INSERT:
                    builder.append('+');
                    break;
                case DELETE:
                    builder.append('-');
                    break;
                default:
                    if (!eq)
                        continue;
                    builder.append(' ');
                    break;
            }
            builder.append(' ').append(diff.line).append('\n');
        }
        return builder.toString();
    }
    
}
