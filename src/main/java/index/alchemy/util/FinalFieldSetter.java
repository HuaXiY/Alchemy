package index.alchemy.util;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Unsafe;
import index.project.version.annotation.Omega;

@Omega
public class FinalFieldSetter {

    private static final FinalFieldSetter INSTANCE = new FinalFieldSetter();
    
    private static final sun.misc.Unsafe unsafe = ReflectionHelper.unsafe();

    @Nullable
    public static FinalFieldSetter instance() {
        return INSTANCE;
    }
    
    @Unsafe(Unsafe.UNSAFE_API)
    public void set(Object obj, Field field, Object value) throws Exception {
        unsafe.putObject(obj, unsafe.objectFieldOffset(field), value);
    }

    @Unsafe(Unsafe.UNSAFE_API)
    public void setStatic(Field field, Object value) throws Exception {
    	unsafe.ensureClassInitialized(field.getDeclaringClass());
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }
    
}