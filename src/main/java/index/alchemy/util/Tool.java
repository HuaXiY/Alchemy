package index.alchemy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

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

	public static final boolean isSubclass(Class<?> supers, Class<?> cls) {
		do
			if (supers == cls)
				return true;
		while ((cls = cls.getSuperclass()) != null);
		return false;
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

	public static final String read(String path, String name) throws Exception {
		return read(path + File.separatorChar + name);
	}

	public static final String read(String path) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "UTF-8"));
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
				content.append(line + "\n");
			return content.toString();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final String read(InputStream in) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					in, "UTF-8"));
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

	public static final String read(File file) throws Exception {
		return read(new FileInputStream(file));
	}

	public static final boolean save(String path, String name, String str) {
		return save(path + File.separatorChar + name, str);
	}

	public static final boolean save(String path, String str) {
		PrintWriter pfp = null;
		try {
			pfp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					path), "utf-8"));
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
		int i = str.indexOf('_');
		return i == -1 ? "" : str.substring(0, i) + str.substring(i + 1, i + 2).toUpperCase() + str.substring(i + 2);
	}
	
}
