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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;

public class Tool {
	
	public static final void where() {
        for (StackTraceElement s : new Throwable().getStackTrace())
            System.err.println(s);
	}
	
	public static final <T extends AccessibleObject> T setAccessible(T t) {
		t.setAccessible(true);
		return t;
	}
	
	public static final String get(String str, String key) {
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
	}
	
	public static final String getDescriptor(Class<?> clazz) {
		if (clazz.isPrimitive())
			return clazz.getName() + ";";
		else
			return "L" + clazz.getName().replace('.', '/') + ";";
	}
	
	@Nullable
	public static final Class forName(String name, boolean init) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static final void init(Class<?> clazz) {
		Object instance = instance(clazz);
		if (instance != null)
			AlchemyInitHook.init(instance);
	}
	
	@Nullable
	public static final <T> T instance(Class<T> clazz) {
		try {
			return (T) setAccessible(clazz.getDeclaredConstructor()).newInstance();
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
		return null;
	}
	
	@Unsafe
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
		try {
			Class.forName(clazz.getName());
		} catch (ClassNotFoundException e) {}
		
		Field field = null;
		try {
			field = clazz.getDeclaredField("type");
		} catch (Exception e) {
			AlchemyModLoader.logger.warn("Can't find Field(type) in: " + clazz.getName());
		}
		if (field != null)
			try {
				FinalFieldSetter.getInstance().setStatic(field, obj);
			} catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
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
	
	public static final <T> T[] toArray(List<T> list, Class<T> type) {
		return list.toArray((T[]) Array.newInstance(type, list.size()));
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
			if (i == supers || getPrimitiveMapping(supers) == clazz)
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
	
	public static final void checkArrayLength(Object array, int length) {
		if (Array.getLength(array) < length)
			AlchemyRuntimeException.onException(new ArrayIndexOutOfBoundsException(length));
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
	
	@Unsafe
	public static final <T> T get(Class cls, int index) {
		return get(cls, index, null);
	}
	
	@Unsafe
	public static final <T> T get(Class cls, int index, Object obj) {
		try {
			return (T) setAccessible(cls.getDeclaredFields()[index]).get(obj);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
			return null;
		}
	}
	
	@Unsafe
	public static final void set(Class cls, int index, Object to) {
		set(cls, index, null, to);
	}
	
	@Unsafe
	public static final void set(Class cls, int index, Object src, Object to) {
		try {
			setAccessible(cls.getDeclaredFields()[index]).set(src, to);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
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
		List<Method> result = new LinkedList<Method>();
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
	
	public static final Void VOID = instance(Void.class);
	
	@Nullable
	public static final Object $(Object... args) throws Exception {
		checkArrayLength(args, 2);
		Object object = args[0].getClass() == Class.class ? null : args[0];
		String name = (String) args[1];
		Class<?> clazz = null;
		if (args[0].getClass() == String.class) {
			String str = (String) args[0];
			if (str.startsWith("L"))
				clazz = Tool.forName(str.substring(1), false);
			if (clazz == null)
				clazz = String.class;
		} else
			clazz = args[0].getClass() == Class.class ? (Class<?>) args[0] : args[0].getClass();
		args = ArrayUtils.subarray(args, 2, args.length);
		do {
			Method method = searchMethod(clazz, name, args);
			if (method != null)
				return isNullOr(method.invoke(object, args), VOID);
		} while((clazz = clazz.getSuperclass()) != null);
		StringBuilder builder = new StringBuilder();
		for (Object arg : args)
			builder.append(arg).append(", ");
		builder.setLength(Math.max(builder.length(), builder.length() - 2));
		throw new RuntimeException("Can't invoke: " + builder.toString());
	}
	
}
