package index.alchemy.util;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

import index.alchemy.api.annotation.Unsafe;
import index.project.version.annotation.Omega;

@Omega
public interface FinalFieldHelper {
    
    static final sun.misc.Unsafe unsafe = $.unsafe();
    
    @Unsafe(Unsafe.UNSAFE_API)
    static void set(@Nullable Object obj, Field field, @Nullable Object value) throws Exception {
        if (obj == null)
            setStatic(field, value);
        else
            unsafe.putObject(obj, unsafe.objectFieldOffset(field), value);
    }
    
    @Unsafe(Unsafe.UNSAFE_API)
    static void setStatic(Field field, @Nullable Object value) throws Exception {
        unsafe.ensureClassInitialized(field.getDeclaringClass());
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }
    
    @Unsafe(Unsafe.UNSAFE_API)
    @SuppressWarnings("unchecked")
    static <T> T get(@Nullable Object obj, Field field) throws Exception {
        if (obj == null)
            return getStatic(field);
        else
            return (T) unsafe.getObject(obj, unsafe.objectFieldOffset(field));
    }
    
    @Unsafe(Unsafe.UNSAFE_API)
    @SuppressWarnings("unchecked")
    static <T> T getStatic(Field field) throws Exception {
        unsafe.ensureClassInitialized(field.getDeclaringClass());
        return (T) unsafe.getObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
    }
    
}