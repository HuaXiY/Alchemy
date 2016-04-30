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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import index.alchemy.core.AlchemyModLoader;

public class Tool {
	
	public static final void where() {
        for (StackTraceElement s : new Throwable().getStackTrace())
            System.err.println(s);
	}
	public static final <Src, To> To proxy(Src src, To to, int i) {
		Field fasrc[] = src.getClass().getDeclaredFields(), fato[] = to
				.getClass().getDeclaredFields();
		int index = -1;
		for (Field fsrc : fasrc) {
			if (++index == i)
				return to;
			if (Modifier.isStatic(fsrc.getModifiers())
					|| Modifier.isFinal(fsrc.getModifiers()))
				continue;
			if (isSubclass(fato[index].getType(), fsrc.getType()))
				try {
					fato[index].setAccessible(true);
					fsrc.setAccessible(true);
					fato[index].set(to, fsrc.get(src));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return to;
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
			if (i == supers)
				return true;
		return isSubclass(supers, clazz);
	}

	public static final PrintWriter getPrintWriter(String path) {
		File file = new File(path);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		try {
			return new PrintWriter(path, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static final PrintWriter getPrintWriter(String path, boolean append) {
		File file = new File(path);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println(file);
				e.printStackTrace();
			}
		try {
			return new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					path, append), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
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
				e.printStackTrace();
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
			e.printStackTrace();
			return false;
		} finally {
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
	
	public static final <T> T isNullOr(T t, T or) {
		return t == null ? or : t;
	}
	
	public static final String isEmptyOr(String str, String or) {
		return str == null || str.isEmpty() ? or : str;
	}
	
	public static final <T> T get(Class cls, int index) {
		return get(cls, index, null);
	}
	
	public static final <T> T get(Class cls, int index, Object obj) {
		Field f = cls.getDeclaredFields()[index];
		f.setAccessible(true);
		try {
			return (T) f.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static final void set(Class cls, int index, Object to) {
		set(cls, index, null, to);
	}
	
	public static final void set(Class cls, int index, Object src, Object to) {
		Field f = cls.getDeclaredFields()[index];
		f.setAccessible(true);
		try {
			f.set(src, to);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final Method searchMethod(Class<?> clazz, Class<?>... args) {
		method_forEach:
		for (Method method : clazz.getDeclaredMethods()) {
			Class ca[] = method.getParameterTypes();
			if (ca.length == args.length) {
				for (int i = 0; i < ca.length; i++) {
					if (ca[i] != args[i])
						continue method_forEach;
				}
				return method;
			}
		}
		throw new RuntimeException("Can't search Method: " + args + ", in: " + clazz);
	}
	
	public static final String _toUp(String str) {
		int i;
		while ((i = str.indexOf('_')) != -1 && str.length() > i + 2)
			str = str.substring(0, i) + str.substring(i + 1, i + 2).toUpperCase() + str.substring(i + 2);
		return str;
	}
	
}
