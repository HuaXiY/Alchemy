package index.alchemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;
@Omega
public final class FinalFieldSetter {

    private static final FinalFieldSetter INSTANCE;

    static {
    	FinalFieldSetter temp = null;
        try {
            temp = new FinalFieldSetter();
        } catch (ReflectiveOperationException e) {
            AlchemyRuntimeException.onException(e);
        }
        INSTANCE = temp;
    }

    private final Object unsafeObj;

    private final Method putObjectMethod;

    private final Method objectFieldOffsetMethod;

    private final Method staticFieldOffsetMethod;

    private final Method staticFieldBaseMethod;

    private FinalFieldSetter() throws ReflectiveOperationException {

        final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

        final Field unsafeField = Tool.setAccessible(unsafeClass.getDeclaredField("theUnsafe"));

        unsafeObj = unsafeField.get(null);

        putObjectMethod = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
        
        objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
        
        staticFieldOffsetMethod = unsafeClass.getMethod("staticFieldOffset", Field.class);
        
        staticFieldBaseMethod = unsafeClass.getMethod("staticFieldBase", Field.class);
    }

    @Nullable
    public static FinalFieldSetter getInstance() {
        return INSTANCE;
    }
    
    public static boolean hasInstance() {
    	return getInstance() != null;
    }

    @Unsafe(Unsafe.UNSAFE_API)
    public void set(final Object o, final Field field, final Object value) throws Exception {
        final Object fieldBase = o;
        final long fieldOffset = (Long) objectFieldOffsetMethod.invoke(unsafeObj, field);

        putObjectMethod.invoke(unsafeObj, fieldBase, fieldOffset, value);
    }

    @Unsafe(Unsafe.UNSAFE_API)
    public void setStatic(final Field field, final Object value) throws Exception {
        final Object fieldBase = staticFieldBaseMethod.invoke(unsafeObj, field);
        final long fieldOffset = (Long) staticFieldOffsetMethod.invoke(unsafeObj, field);

        putObjectMethod.invoke(unsafeObj, fieldBase, fieldOffset, value);
    }
}