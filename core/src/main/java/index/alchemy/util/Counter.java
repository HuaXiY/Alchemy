package index.alchemy.util;

import java.util.function.BooleanSupplier;

import index.project.version.annotation.Omega;

@Omega
public class Counter implements BooleanSupplier {
    
    public int count, max;
    
    public Counter(int max) {
        this.max = max;
    }
    
    @Override
    public boolean getAsBoolean() {
        if (++count >= max) {
            count = 0;
            return true;
        }
        return false;
    }
    
}
