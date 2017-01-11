package index.alchemy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.RegEx;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;

@Omega
public class Tool {
	
	public static final StackTraceElement[] getStackTrace() {
		return new Throwable().getStackTrace();
	}
	
	public static final void where() {
		for (StackTraceElement s : getStackTrace())
			System.err.println(s);
	}
	
	public static final <T> T cloneArray(T array) {
		if (array == null || !array.getClass().isArray())
			throw new IllegalArgumentException(toString(array));
		if (array instanceof Object[])
			return (T) ((Object[]) array).clone();
		if (array instanceof byte[])
			return (T) ((byte[]) array).clone();
		if (array instanceof short[])
			return (T) ((short[]) array).clone();
		if (array instanceof int[])
			return (T) ((int[]) array).clone();
		if (array instanceof long[])
			return (T) ((long[]) array).clone();
		if (array instanceof char[])
			return (T) ((char[]) array).clone();
		if (array instanceof float[])
			return (T) ((float[]) array).clone();
		if (array instanceof double[])
			return (T) ((double[]) array).clone();
		if (array instanceof boolean[])
			return (T) ((boolean[]) array).clone();
		throw new AssertionError(toString(array));
	}
	
	public static final int deepHashCode(@Nonnull Object obj) {
		if (obj instanceof Object[])
			return Arrays.deepHashCode((Object[]) obj);
		if (obj instanceof byte[])
			return Arrays.hashCode((byte[]) obj);
		if (obj instanceof short[])
			return Arrays.hashCode((short[]) obj);
		if (obj instanceof int[])
			return Arrays.hashCode((int[]) obj);
		if (obj instanceof long[])
			return Arrays.hashCode((long[]) obj);
		if (obj instanceof char[])
			return Arrays.hashCode((char[]) obj);
		if (obj instanceof float[])
			return Arrays.hashCode((float[]) obj);
		if (obj instanceof double[])
			return Arrays.hashCode((double[]) obj);
		if (obj instanceof boolean[])
			return Arrays.hashCode((boolean[]) obj);
		return obj.hashCode();
	}
	
	public static final boolean deepEquals(Object a, Object b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		if (a.getClass().isArray() && b.getClass().isArray())
			return deepArrayEquals((Object[]) a, (Object[]) b);
		return a.equals(b);
	}
	
	public static final boolean deepArrayEquals(Object a, Object b) {
		if (a instanceof Object[] && b instanceof Object[])
			return Arrays.deepEquals((Object[]) a, (Object[]) b);
		if (a instanceof byte[] && b instanceof byte[])
			return Arrays.equals((byte[]) a, (byte[]) b);
		if (a instanceof short[] && b instanceof short[])
			return Arrays.equals((short[]) a, (short[]) b);
		if (a instanceof int[] && b instanceof int[])
			return Arrays.equals((int[]) a, (int[]) b);
		if (a instanceof long[] && b instanceof long[])
			return Arrays.equals((long[]) a, (long[]) b);
		if (a instanceof char[] && b instanceof char[])
			return Arrays.equals((char[]) a, (char[]) b);
		if (a instanceof float[] && b instanceof float[])
			return Arrays.equals((float[]) a, (float[]) b);
		if (a instanceof double[] && b instanceof double[])
			return Arrays.equals((double[]) a, (double[]) b);
		if (a instanceof boolean[] && b instanceof boolean[])
			return Arrays.equals((boolean[]) a, (boolean[]) b);
		return a.equals(b);
	}
	
	public static final boolean equals(Object src, Object... objs) {
		for (Object obj : objs)
			if (Objects.equals(src, obj))
				return true;
		return false;
	}
	
	public static final String toString(Object obj) {
		if (obj == null)
			return "null";
		Class clazz = obj.getClass();
		if (!clazz.isArray())
			return obj.toString();
		if (clazz == byte[].class)
			return Arrays.toString((byte[]) obj);
		if (clazz == char[].class)
			return Arrays.toString((char[]) obj);
		if (clazz == double[].class)
			return Arrays.toString((double[]) obj);
		if (clazz == float[].class)
			return Arrays.toString((float[]) obj);
		if (clazz == int[].class)
			return Arrays.toString((int[]) obj);
		if (clazz == long[].class)
			return Arrays.toString((long[]) obj);
		if (clazz == short[].class)
			return Arrays.toString((short[]) obj);
		if (clazz == boolean[].class)
			return Arrays.toString((boolean[]) obj);
		return Arrays.deepToString((Object[]) obj);
	}
	
	public static final String makeString(char c, int len) {
		if (len < 1)
			return "";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < len; i++)
			builder.append(c);
		return builder.toString();
	}
	
	public static final void coverString(String src, String to) {
		try {
			Field value = setAccessible(String.class.getDeclaredField("value"));
			FinalFieldSetter.instance().set(src, value, value.get(to));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final <T extends AccessibleObject> T setAccessible(T t) {
		t.setAccessible(true);
		return t;
	}
	
	public static final int getRandom(int min, int max) {
		return AlchemyModLoader.random.nextInt(max - min + 1) + min;
	}
	
	public static final String get(String str, @RegEx String key) {
		Matcher matcher = Pattern.compile(key).matcher(str);
		if (matcher.find())
			return matcher.group(1);
		return "";
	}
	
	public static final String decode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}
	
	public static final void addURLToClassLoader(ClassLoader loader, URL url) throws Exception {
		if (loader instanceof URLClassLoader)
			$(loader, "addURL", url);
		else if (loader.getParent() != null)
			addURLToClassLoader(loader.getParent(), url);
	}
	
	@Nullable
	public static final Class forName(String name, boolean init) {
		if (name == null || name.isEmpty())
			return null;
		try {
			return Class.forName(name, init, Tool.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static final void load(Class<?> clazz) {
		try {
			Class.forName(clazz.getName());
		} catch (ClassNotFoundException e) {
			AlchemyRuntimeException.onException(e);
		}
	}
	
	public static final void init(Class<?> clazz) {
		Object instance = instance(clazz);
		if (instance != null)
			AlchemyInitHook.init(instance);
	}
	
	public static final <T> T instance(Class<T> clazz) {
		try {
			return (T) setAccessible(clazz.getDeclaredConstructor()).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final <T> T instance(Class<T> clazz, Object obj) {
		try {
			return (T) setAccessible(clazz.getDeclaredConstructor(obj.getClass())).newInstance(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final <T extends Annotation> T makeAnnotation(Class<T> clazz, List<Object> objects, Object... others) {
		return makeAnnotation(clazz, null, objects, others);
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final <T extends Annotation> T makeAnnotation(Class<T> clazz, @Nullable Map<String, InvocationHandler> mapProxy,
			List<Object> objects, Object... others) {
		objects.addAll(Arrays.asList(others));
		Map<String, Object> args = new HashMap<String, Object>();
		if (objects.size() % 2 != 0)
			AlchemyRuntimeException.onException(new RuntimeException("objects.size() % 2 != 0"));
		else {
			String temp = null;
			for (Object obj : objects)
				if (temp == null)
					if (obj instanceof String)
						temp = (String) obj;
					else
						AlchemyRuntimeException.onException(new RuntimeException("object is not a string"));
				else {
					if (!args.containsKey(temp)) {
						try {
							Class<?> returnType = clazz.getMethod(temp).getReturnType();
							if (!Tool.isInstance(returnType, obj.getClass()) && obj.getClass() == String[].class) {
								String sa[] = (String[]) obj;
								obj = setAccessible(forName(sa[0].substring(1, sa[0].length() - 1).replace('/', '.'), true)
										.getDeclaredField(sa[1])).get(null);
							}
						} catch (Exception e) {
							AlchemyRuntimeException.onException(e);
						}
						args.put(temp, obj);
					}
					temp = null;
				}
		}
		return AnnotationInvocationHandler.make(clazz, args, mapProxy);
		//return (T) sun.reflect.annotation.AnnotationParser.annotationForMap(clazz, args);
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final <Src, To> To proxy(Src src, To to, int i) {
		Field fasrc[] = src.getClass().getDeclaredFields(), fato[] = to.getClass().getDeclaredFields();
		int index = -1;
		for (Field fsrc : fasrc) {
			if (++index == i)
				return to;
			if (Modifier.isStatic(fsrc.getModifiers()) || Modifier.isFinal(fsrc.getModifiers()))
				continue;
			if (isSubclass(fato[index].getType(), fsrc.getType()))
				try {
					setAccessible(fato[index]).set(to, setAccessible(fsrc).get(src));
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
		}
		return to;
	}
	
	public static final void setType(Class<?> clazz, Object obj) {
		// Initialize this class, the initialization process will assign fields to null
		load(clazz);
		try {
			Field field = clazz.getDeclaredField("type");
			FinalFieldSetter.instance().setStatic(field, obj);
		} catch (Exception e) { }
	}
	
	public static final void checkInvokePermissions(int deep, Class<?> clazz) {
		StackTraceElement ea[];
		for (StackTraceElement element : ea = new Throwable().getStackTrace())
			if (clazz.getName().equals(element.getClassName()))
				return;
		AlchemyRuntimeException.onException(new IllegalAccessException(
				ea[deep].getClassName() + " can't invoke " +
				ea[deep - 1].getClassName() + "#" + ea[deep - 1].getMethodName()));
	}
	
	private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING = new HashMap<Class<?>, Class<?>>();
	static {
		PRIMITIVE_MAPPING.put(byte.class, Byte.class);
		PRIMITIVE_MAPPING.put(short.class, Short.class);
		PRIMITIVE_MAPPING.put(int.class, Integer.class);
		PRIMITIVE_MAPPING.put(long.class, Long.class);
		PRIMITIVE_MAPPING.put(float.class, Float.class);
		PRIMITIVE_MAPPING.put(double.class, Double.class);
		PRIMITIVE_MAPPING.put(boolean.class, Boolean.class);
		PRIMITIVE_MAPPING.put(char.class, Character.class);
	}
	
	private static final Class<?>[] SIMPLE = new Class[]{ Character.class, String.class, Boolean.class,
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class };
	
	public static final Class<?> getPrimitiveMapping(Class<?> clazz) {
		return PRIMITIVE_MAPPING.get(clazz);
	}
	
	public static final boolean isPacking(Class<?> clazz){
		return ArrayUtils.contains(SIMPLE, clazz);
	}
	
	public static final boolean isSimple(Class<?> clazz){
		return clazz.isPrimitive() || isPacking(clazz);
	}
	
	public static final boolean isBasics(Class<?> clazz){
		return isSimple(clazz) || clazz == String.class;
	}

	public static final boolean isSubclass(Class<?> supers, Class<?> clazz) {
		do
			if (supers == clazz)
				return true;
		while ((clazz = clazz.getSuperclass()) != null);
		return false;
	}
	
	public static final boolean isInstance(Class<?> supers, Class<?> clazz) {
		for (Class<?> i : clazz.getInterfaces())
			if (isInstance(supers, i) || getPrimitiveMapping(supers) == clazz)
				return true;
		return isSubclass(supers, clazz);
	}
	
	public static final int getSafe(int[] array, int i, int def) {
		return i < 0 || i > array.length - 1 ? def : array[i];
	}
	
	public static final <T> T getSafe(T[] array, int i, T def) {
		return i < 0 || i > array.length - 1 ? def : array[i];
	}
	
	public static final <T> T getSafe(List<T> list, int i) {
		return i < 0 || i > list.size() - 1 ? null : list.get(i);
	}
	
	public static final void checkNull(Object... args) {
		for (Object obj : args)
			if (obj == null)
				AlchemyRuntimeException.onException(new NullPointerException());
	}
	
	public static final boolean nonNull(Object... args) {
		for (Object obj : args)
			if (obj == null)
				return false;
		return true;
	}
	
	public static final void checkArrayLength(Object array, int length) {
		if (Array.getLength(array) < length)
			AlchemyRuntimeException.onException(new ArrayIndexOutOfBoundsException(length));
	}
	
	public static final boolean checkDir(String path) {
		File file = new File(path);
		return file.isDirectory() || file.mkdirs();
	}

	@Nullable
	public static final PrintWriter getPrintWriter(String path) {
		File file = new File(path);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				AlchemyRuntimeException.onException(e);
			}
		try {
			return new PrintWriter(path, "utf-8");
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			return null;
		}
	}

	@Nullable
	public static final PrintWriter getPrintWriter(String path, boolean append) {
		File file = new File(path);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				AlchemyRuntimeException.onException(e);
			}
		try {
			return new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					path, append), "utf-8"));
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			return null;
		}
	}
	
	public static final String readSafe(File file) {
		try {
			return read(file);
		} catch (IOException e) {
			AlchemyModLoader.logger.error("Can't read: " + file.getPath());
			AlchemyModLoader.logger.error(e.getMessage());
			return "";
		}
	}

	public static final String read(File file) throws IOException {
		return read(new FileInputStream(file));
	}
	
	public static final String read(InputStream in) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
				content.append(line + "\n");
			return content.toString();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				AlchemyRuntimeException.onException(e);
			}
		}
	}
	
	public static final boolean saveSafe(File file, String str) {
		try {
			return save(file, str);
		} catch (IOException e) {
			AlchemyModLoader.logger.error("Can't save: " + file.getPath());
			AlchemyModLoader.logger.error(e.getMessage());
			return false;
		}
	}

	public static final boolean save(File file, String str) throws IOException {
		return save(new FileOutputStream(file), str);
	}

	public static final boolean save(FileOutputStream out, String str) {
		PrintWriter pfp = null;
		try {
			pfp = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
			pfp.print(str);
			return true;
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			return false;
		} finally {
			if (pfp != null)
				pfp.close();
		}
	}
	
	public static final void getAllFile(File f, List<String> list){
		if (f.exists())
			if (f.isDirectory()) {
				File fa[] = f.listFiles();
				if (fa == null)
					return;
				for (File ft : fa)
					getAllFile(ft, list);
			} else
				list.add(f.getPath());
	}
	
	public static final void getAllURL(File f, List<URL> list) throws MalformedURLException {
		if (f.exists())
			if (f.isDirectory()) {
				File fa[] = f.listFiles();
				if (fa == null)
					return;
				for (File ft : fa)
					getAllURL(ft, list);
			} else
				list.add(f.toURI().toURL());
	}
	
	public static final <T> T isNullOr(T t, T or) {
		return t == null ? or : t;
	}
	
	public static final String isEmptyOr(String str, String or) {
		return str == null || str.isEmpty() ? or : str;
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final <T> T get(Class cls, int index) {
		return get(cls, index, null);
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final <T> T get(Class cls, int index, Object obj) {
		try {
			return (T) setAccessible(cls.getDeclaredFields()[index]).get(obj);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			return null;
		}
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final void set(Class cls, int index, Object to) {
		set(cls, index, null, to);
	}
	
	@Unsafe(Unsafe.REFLECT_API)
	public static final void set(Class cls, int index, Object src, Object to) {
		try {
			setAccessible(cls.getDeclaredFields()[index]).set(src, to);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
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
			Class ca[] = method.getParameterTypes();
			if (ca.length == args.length) {
				for (int i = 0; i < ca.length; i++) {
					if (ca[i] != args[i])
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
					if (!isInstance(now_args[i], args[i].getClass()))
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
	
	public static final String _ToUpper(String str) {
		boolean upper = false;
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray())
			if (c == '_')
				upper = true;
			else if (upper) {
				builder.append(Character.toUpperCase(c));
				upper = false;
			} else
				builder.append(c);
		return builder.toString();
	}
	
	public static final String upperTo_(String str) {
		boolean lower = false;
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray())
			if (Character.isLowerCase(c)) {
				builder.append('_').append(Character.toLowerCase(c));
			} else
				builder.append(c);
		return builder.toString();
	}
	
	public static final int[] stringToIntArray(String str) throws NumberFormatException {
		String sa[] = str.split(".");
		int [] result = new int[sa.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Integer.valueOf(sa[i]);
		return result;
	}
	
	public static final String getString(char c, int len) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < len; i++)
			builder.append(c);
		return builder.toString();
	}
	
	public static final Map<String, String> getMapping(String str) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String line : str.split("\n")) {
			String sa[] = line.split("=");
			if (sa.length == 2)
				result.put(sa[0], sa[1]);
		}
		return result;
	}
	
	public static final Void VOID = null;//instance(Void.class);
	
	@Nullable
	@Unsafe(Unsafe.REFLECT_API)
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
			if (args.length == 1)
				return (T) clazz;
			String name = (String) args[1];
			Object object = args[0].getClass() == clazz ? args[0] : null;
			try {
				if (args.length == 2)
					return (T) setAccessible(searchField(clazz, (String) args[1])).get(object);
				if (args.length == 3 && ((String) args[1]).endsWith("<")) {
					Field field = setAccessible(searchField(clazz, ((String) args[1]).replace("<", "")));
					field.set(object, args[2]);
					return (T) args[2];
				}
			} catch (NoSuchFieldException e) { }
			args = ArrayUtils.subarray(args, 2, args.length);
			if (name.equals("new")) {
				method_forEach:
				for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
					Class<?> now_args[] = constructor.getParameterTypes();
					if (now_args.length == args.length) {
						for (int i = 0; i < args.length; i++)
							if (!isInstance(now_args[i], args[i].getClass()))
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
			throw new RuntimeException("Can't invoke: " + Joiner.on(',').join(args), e);
		}
	}
	
	public static final void printClass(String name) {
		try {
			new ClassReader(name).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final void printClass(byte[] code) {
		new ClassReader(code).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
	}
	
	public static final void dumpClass(byte[] code, String path) {
		new ClassReader(code).accept(new TraceClassVisitor(new PrintWriter(getPrintWriter(path))), 0);
	}
	
}
