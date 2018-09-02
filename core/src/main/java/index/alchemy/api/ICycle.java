package index.alchemy.api;

import javax.annotation.Nullable;

import static index.alchemy.util.$.$;

@FunctionalInterface
public interface ICycle {
    
    float update(int tick);
    
    default float next() { return update(1); }
    
    @Nullable
    default ICycle copy() {
        return $(getClass(), "new");
    }
    
}