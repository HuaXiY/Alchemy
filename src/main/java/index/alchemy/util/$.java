package index.alchemy.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import index.alchemy.api.annotation.Unsafe;


public class $ {
	
	private static sun.misc.Unsafe unsafe;
	
	private static long overrideOffset;
	
	private static Void voidInstance;
	
	public static Void voidInstance() { return voidInstance; }
	
	protected static void markUnsafe(sun.misc.Unsafe unsafe) {
		if (unsafe != null) {
			$.unsafe = unsafe;
			try {
				voidInstance = (Void) unsafe.allocateInstance(Void.class);
				Field override = AccessibleObject.class.getDeclaredField("override");
				overrideOffset = unsafe.objectFieldOffset(override);
			} catch (Exception e) { throw new RuntimeException(e); }
		}
	}
	
//	public static void unnameClassLoader(ClassLoader classLoader) {
//		unnameModule(classLoader.getUnnamedModule());
//	}
//	
//	public static void unnamePackage(Package pkg) {
//		unnameModule($(pkg, "module"));
//	}
//	
//	public static void unnameModule(Module module) {
////		$(module, "implAddReadsAllUnnamed");
////		$(module, "name<", null);
////		final Module EVERYONE_MODULE = $(Module.class, "EVERYONE_MODULE");
////		final Map<String, Set<Module>>
////				openPackages = $.<Map<String, Set<Module>>>$(module, "openPackages"),
////				exportedPackages = $.<Map<String, Set<Module>>>$(module, "exportedPackages");
//		module.getPackages().forEach(packageName -> {
//			$(module, "implAddOpens", packageName);
////			unnameModule(packageName, openPackages, EVERYONE_MODULE);
////			unnameModule(packageName, exportedPackages, EVERYONE_MODULE);
//		});
//	}
//	
//	public static void unnameModule(String packageName, Map<String, Set<Module>> packageMapping, Module target) {
//		Set<Module> modules = packageMapping.get(packageName);
//		if (modules == null)
//			packageMapping.put(packageName, Sets.newHashSet(target));
//		else
//			packageMapping.put(packageName, Sets.newHashSet(target));
//	}
	
	private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING = Maps.newHashMap();
	static {
		PRIMITIVE_MAPPING.put(byte.class, Byte.class);
		PRIMITIVE_MAPPING.put(short.class, Short.class);
		PRIMITIVE_MAPPING.put(int.class, Integer.class);
		PRIMITIVE_MAPPING.put(long.class, Long.class);
		PRIMITIVE_MAPPING.put(float.class, Float.class);
		PRIMITIVE_MAPPING.put(double.class, Double.class);
		PRIMITIVE_MAPPING.put(boolean.class, Boolean.class);
		PRIMITIVE_MAPPING.put(char.class, Character.class);
		PRIMITIVE_MAPPING.put(void.class, Void.class);
	}
	
	private static final Class<?>[] SIMPLE = new Class[]{ Character.class, String.class, Boolean.class,
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class };
	
	public static final Map<Class<?>, Class<?>> getPrimitiveMapping() {
		return PRIMITIVE_MAPPING;
	}
	
	public static final Class<?> getPrimitiveMapping(Class<?> clazz) {
		return PRIMITIVE_MAPPING.get(clazz);
	}
	
	public static final boolean isPacking(Class<?> clazz) {
		return ArrayUtils.contains(SIMPLE, clazz);
	}
	
	public static final boolean isSimple(Class<?> clazz) {
		return clazz.isPrimitive() || isPacking(clazz);
	}
	
	public static final boolean isBasics(Class<?> clazz) {
		return isSimple(clazz) || clazz == String.class;
	}

	public static final boolean isSubclass(Class<?> supers, Class<?> clazz) {
		do
			if (supers == clazz)
				return true;
		while ((clazz = clazz.getSuperclass()) != null);
		return false;
	}
	
	public static final <T extends AccessibleObject> T setAccessible(T accessible) {
		unsafe.putBoolean(accessible, overrideOffset, true);
		return accessible;
	}
	
	public static final boolean isInstance(Class<?> supers, Class<?> clazz) {
		return supers.isAssignableFrom(clazz) || getPrimitiveMapping(supers) == clazz;
	}
	
	public static final List<Field> getAllFields(Class<?> clazz) {
		List<Field> result = Lists.newArrayList();
		do
			result.addAll(Arrays.asList(clazz.getDeclaredFields()));
		while ((clazz = clazz.getSuperclass()) != null);
		return result;
	}
	
	public static final Field searchField(Class<?> clazz, String name) throws NoSuchFieldException {
		for (Field field : getAllFields(clazz))
			if (field.getName().equals(name))
				return field;
		throw new NoSuchFieldException(clazz + ": " + name);
	}
	
	@Nullable
	public static final Method searchMethod(Class<?> clazz, Class<?>... args) {
		method_forEach:
		for (Method method : clazz.getDeclaredMethods()) {
			Class<?> ca[] = method.getParameterTypes();
			if (ca.length == args.length) {
				for (int i = 0; i < ca.length; i++) {
					if (!isInstance(ca[i], args[i] == null ? Object.class : args[i]))
						continue method_forEach;
				}
				return setAccessible(method);
			}
		}
		return null;
	}
	
	@Nullable
	public static final Method searchMethod(Class<?> clazz, String name, Object... args) {
		method_forEach:
		for (Method method : clazz.getDeclaredMethods()) {
			Class<?> now_args[] = method.getParameterTypes();
			if (method.getName().equals(name) && now_args.length == args.length) {
				for (int i = 0; i < args.length; i++)
					if (!isInstance(now_args[i], args[i] == null ? Object.class : args[i].getClass()))
						continue method_forEach;
				return setAccessible(method);
			}
		}
		return null;
	}
	
	public static final List<Method> searchMethod(Class<?> clazz, String name) {
		List<Method> result = Lists.newArrayList();
		for (Method method : clazz.getDeclaredMethods())
			if (method.getName().equals(name))
				result.add(setAccessible(method));
		return result;
	}
	
	@Nullable
	public static final Class<?> forName(String name) {
		return forName(name, false);
	}
	
	@Nullable
	public static final Class<?> forName(String name, boolean init) {
		if (name == null || name.isEmpty())
			return null;
		try {
			return Class.forName(name, init, Tool.class.getClassLoader());
		} catch (ClassNotFoundException e) { return null; }
	}

	@Nullable
	@Unsafe(Unsafe.REFLECT_API)
	@SuppressWarnings("unchecked")
	public static final <T> T $(Object... args) {
		try {
			if (args.length < 1)
				return null;
			Class<?> clazz = null;
			if (args[0].getClass() == String.class) {
				String str = (String) args[0];
				if (str.startsWith("L"))
					clazz = forName(str.substring(1), false);
				if (clazz == null)
					clazz = String.class;
			} else
				clazz = args[0].getClass() == Class.class ? (Class<?>) args[0] : args[0].getClass();
			unsafe.ensureClassInitialized(clazz);
			if (args.length == 1)
				return (T) clazz;
			String name = (String) args[1];
			Object object = args[0].getClass() == clazz ? args[0] : null;
			if (!name.startsWith(">"))
				try {
					if (args.length == 2)
						return (T) setAccessible(searchField(clazz, (String) args[1])).get(object);
					if (args.length == 3 && ((String) args[1]).endsWith("<")) {
						Field field = setAccessible(((String) args[1]).endsWith("<<") ?
								clazz.getDeclaredField(((String) args[1]).replace("<", "")) :
									searchField(clazz, ((String) args[1]).replace("<", "")));
						if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
							if (object == null)
								FinalFieldSetter.instance().setStatic(field, args[2]);
							else
								FinalFieldSetter.instance().set(object, field, args[2]);
						else
							field.set(object, args[2]);
						return (T) args[2];
					}
				} catch (NoSuchFieldException e) { }
			name = name.replace(">", "");
			args = ArrayUtils.subarray(args, 2, args.length);
			if (name.equals("new")) {
				method_forEach:
				for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
					Class<?> now_args[] = constructor.getParameterTypes();
					if (now_args.length == args.length) {
						for (int i = 0; i < args.length; i++)
							if (args[i] != null ? !isInstance(now_args[i], args[i].getClass()) : now_args[i].isPrimitive())
								continue method_forEach;
						setAccessible(constructor);
						return (T) constructor.newInstance(args);
					}
				}
			}
			do {
				Method method = searchMethod(clazz, name, args);
				if (method != null)
					return (T) method.invoke(object, args);
			} while((clazz = clazz.getSuperclass()) != null);
			throw new IllegalArgumentException();
		} catch(Exception e) {
			throw new RuntimeException("Can't invoke(" + args.length + "): " + Joiner.on(',').useForNull("null").join(args), e);
		}
	}
	
}
