package index.alchemy.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Beta;

@Beta
public class ReflectionHelper {
	
	static { resetReflection(); }
	
	protected static final Field refData = getField(Class.class, "reflectionData");
	
	static { FunctionHelper.always(ReflectionHelper::resetReflectionData, Class.class, System.class, ReflectionHelper.class); }
	
	protected static final Field classLoader = getField(Class.class, "classLoader"), security = getField(System.class, "security");
	
	static { setAccessible(ReflectionHelper.class); }
	
	private static final sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
	
	public static final sun.misc.Unsafe getUnsafe() {
		return unsafe;
	}
	
	public static final void resetReflectionData(Class<?> clazz) {
		set(refData, clazz, null);
	}
	
	public static final void resetReflection() {
		for(Field f : (Field[]) invoke(getMethod(Class.class, "getDeclaredFields0", boolean.class), sun.reflect.Reflection.class, false))
			if (f.getType() == Map.class)
				set(setAccessible(f), sun.reflect.Reflection.class, Maps.newHashMap());
	}
	
	public static final boolean canSetAccessible() {
		try {
			return getField(Class.class, "classLoader") != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static final void setSecurityManager(SecurityManager manager) {
		set(security, manager);
	}
	
	public static final void setAccessible(Class<?> clazz) {
		setClassLoader(clazz, null);
	}
	
	public static final void setClassLoader(Class<?> clazz, ClassLoader loader) {
		set(classLoader, clazz, loader);
	}
	
	public static final <T extends AccessibleObject> T setAccessible(T accessible) {
		accessible.setAccessible(true);
		return accessible;
	}
	
	@Nullable
	public static final <T> T allocateInstance(Class<T> clazz) {
		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) { AlchemyRuntimeException.onException(e); }
		return null;
	}
	
	@Nullable
	public static final Field getField(Class<?> owner, String name) {
		Class<?> clazz = owner;
		while (clazz != null)
			try {
				return setAccessible(clazz.getDeclaredField(name));
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
				continue;
			}
		AlchemyRuntimeException.onException(new NoSuchFieldException(owner.getName() + "." + name));
		return null;
	}
	
	public static final void set(Field field, Object obj, Object val) {
		try {
			field.set(obj, val);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}
	
	public static final void set(Field field, Object val) {
		set(field, null, val);
	}
	
	@Nullable
	public static final <T> T get(Field field, Object obj) {
		try {
			return (T) field.get(obj);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
		return null;
	}
	
	@Nullable
	public static final <T> T get(Field field) {
		return get(field, null);
	}
	
	@Nullable
	public static final Method getMethod(Class<?> owner, String name, Class... args) {
		Class<?> clazz = owner;
		while (clazz != null)
			try {
				return setAccessible(clazz.getDeclaredMethod(name, args));
			} catch (NoSuchMethodException e) {
				clazz = clazz.getSuperclass();
				continue;
			}
		AlchemyRuntimeException.onException(new NoSuchMethodException(owner.getName() + "#" + name));
		return null;
	}
	
	@Nullable
	public static final <R> R invoke(Method method, Object obj, Object... args) {
		try {
			return (R) method.invoke(obj, args);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
		return null;
	}
	
}
