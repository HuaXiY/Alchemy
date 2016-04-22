package index.alchemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FinalFieldSetter {

    private static final FinalFieldSetter INSTANCE;

    static {
        try {
            INSTANCE = new FinalFieldSetter();
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Object unsafeObj;

    private final Method putObjectMethod;

    private final Method objectFieldOffsetMethod;

    private final Method staticFieldOffsetMethod;

    private final Method staticFieldBaseMethod;

    private FinalFieldSetter() throws ReflectiveOperationException {

        final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

        final Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);

        unsafeObj = unsafeField.get(null);

        putObjectMethod = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
        
        objectFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
        
        staticFieldOffsetMethod = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
        
        staticFieldBaseMethod = unsafeClass.getDeclaredMethod("staticFieldBase", Field.class);
    }

    public static FinalFieldSetter getInstance() {
        return INSTANCE;
    }

    public void set(final Object o, final Field field, final Object value) throws Exception {

        final Object fieldBase = o;
        final long fieldOffset = (Long) objectFieldOffsetMethod.invoke(unsafeObj, field);

        putObjectMethod.invoke(unsafeObj, fieldBase, fieldOffset, value);
    }

    public void setStatic(final Field field, final Object value) throws Exception {

        final Object fieldBase = staticFieldBaseMethod.invoke(unsafeObj, field);
        final long fieldOffset = (Long) staticFieldOffsetMethod.invoke(unsafeObj, field);

        putObjectMethod.invoke(unsafeObj, fieldBase, fieldOffset, value);
    }
}