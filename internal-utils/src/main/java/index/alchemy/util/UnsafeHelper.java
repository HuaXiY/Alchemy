package index.alchemy.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import sun.misc.Unsafe;

public class UnsafeHelper {
    
    private static final sun.misc.Unsafe unsafe = FunctionHelper.onThrowableSupplier(UnsafeHelper::getUnsafe, FunctionHelper::rethrowVoid).get();
    
    private static sun.misc.Unsafe getUnsafe() throws PrivilegedActionException {
        return AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        });
    }
    
    public static sun.misc.Unsafe unsafe() {
        return unsafe;
    }
    
}
