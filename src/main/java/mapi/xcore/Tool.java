package mapi.xcore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.*;

public class Tool {
	public static final class Fields {
		public static final Field setAccessible(Field f){
			f.setAccessible(true);
			return f;
		}
		public static final Field getField(Class<?> clazz, int index){
			return setAccessible(clazz.getDeclaredFields()[index]);
		}
		public static final Field getField(Class<?> clazz, String name){
			try {
				return setAccessible(asList(getAll(clazz)).stream().filter(f -> f.getName().equals(name)).findFirst().get());
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static final <T> T get(Field f) {
			return get(f, null);
		}
		@SuppressWarnings("unchecked")
		public static final <T> T get(Field f, Object obj){
			try {
				return (T) setAccessible(f).get(obj);
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static final void set(Field f, Object val) {
			set(f, null, val);
		}
		public static final void set(Field f, Object obj, Object val){
			try {
				setAccessible(f).set(obj, val);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		public static final <T> T get(Class<?> clazz, int index) {
			return get(clazz, index, null);
		}
		@SuppressWarnings("unchecked")
		public static final <T> T get(Class<?> clazz, int index, Object obj) {
			try {
				return (T) setAccessible(clazz.getDeclaredFields()[index]).get(obj);
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static final void set(Class<?> clazz, int index, Object to) {
			set(clazz, index, null, to);
		}
		public static final void set(Class<?> clazz, int index, Object src, Object to) {
			try {
				setAccessible(clazz.getDeclaredFields()[index]).set(src, to);
			} catch(Exception e){e.printStackTrace();}
		}
		public static final <T> T get(Class<?> clazz, String name) {
			return get(clazz, name, null);
		}
		@SuppressWarnings("unchecked")
		public static final <T> T get(Class<?> clazz, String name, Object obj) {
			try {
				return (T) setAccessible(clazz.getDeclaredField(name)).get(obj);
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static final void set(Class<?> clazz, String name, Object to) {
			set(clazz, name, null, to);
		}
		public static final void set(Class<?> clazz, String name, Object src, Object to) {
			try {
				setAccessible(clazz.getDeclaredField(name)).set(src, to);
			} catch(Exception e){e.printStackTrace();}
		}
		public static final Field[] getAll(Class<?> clazz){
			List<Field[]> lfa = new LinkedList<Field[]>();
			do lfa.add(clazz.getDeclaredFields());
			while((clazz = clazz.getSuperclass()) != null);
			return Arrays.merge(lfa.toArray(new Field[lfa.size()][]));
		}
		public static final void execute(Field f, Object obj, String value){
			try {
				f.setAccessible(true);
				switch(f.getType().getName()){
					case "boolean":f.setBoolean(obj, Boolean.valueOf(value));break;
					case "byte":f.setByte(obj, Byte.valueOf(value));break;
					case "short":f.setShort(obj, Short.valueOf(value));break;
					case "int":f.setInt(obj, Integer.valueOf(value));break;
					case "long":f.setLong(obj, Long.valueOf(value));break;
					case "float":f.setFloat(obj, Float.valueOf(value));break;
					case "double":f.setDouble(obj, Double.valueOf(value));break;
					case "char":f.setChar(obj, value.charAt(0));break;
				}
			} catch (Exception e){}
		}
	}
	public static final class Methods {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final <T> T generate(final Method m, Class<T> clazz, final Consumer<Throwable> error) {
			if(clazz == Function.class)return (T)(Function) o -> {
				try {return m.invoke(o);}
				catch(Throwable t){
					error.accept(t);
					return null;
				}
			};
			else if(clazz == BiFunction.class)return (T)(BiFunction) (o, a1) -> {
				try {return m.invoke(o, a1);}
				catch(Throwable t){
					error.accept(t);
					return null;
				}
			};
			else return null;
		}
		public static final Method setAccessible(Method m){
			m.setAccessible(true);
			return m;
		}
		public static final Method getMethod(Class<?> clazz, int index){
			try {
				return setAccessible(clazz.getDeclaredMethods()[index]);
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static final Method getMethod(Class<?> clazz, String name, Class<?>... args){
			try {
				return setAccessible(clazz.getDeclaredMethod(name, args));
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	public static final class Strings {
		@SuppressWarnings("resource")
		public static final String getFromStdIn() {
			return new Scanner(System.in).nextLine();
		}
		public static final PrintWriter getPrintWriter(String path){
			Files.checkDir(Files.getParent(path));
			try {return new PrintWriter(path, "UTF-8");}
			catch(Exception e){return null;}
		}
		public static final PrintWriter getPrintWriter(String path, boolean append){
			Files.checkDir(Files.getParent(path));
			try {return new PrintWriter(new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"));}
			catch(Exception e){return null;}
		}
		public static final String read(String path, String name){
			return read(path + File.separatorChar + name);
		}
		public static final String read(String path){
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))){
				StringBuilder content = new StringBuilder();
				String line;
				while((line = reader.readLine()) != null)content.append(line).append("\n");
				return content.toString();
			} catch(Exception e){return "";}
		}
		public static final boolean save(String path, String name, String str){
			return save(path + File.separatorChar + name, str);
		}
		public static final boolean save(String path, String str){
			Files.checkDir(Files.getParent(path));
			try(PrintWriter pfp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))){
				for(String string : str.split("\n"))pfp.println(string);
				return true;
			} catch(Exception e){e.printStackTrace();return false;}
		}
		public static final boolean contains(String str, char c){
			for(char t : str.toCharArray())if(t == c)return true;
			return false;
		} 
		public static final String get(String str, String key){
			Matcher m = Pattern.compile(key).matcher(str);
			if(m.find())return m.group(1);
			return null;
		}
		public static final List<String> getAll(String str, String key){
			List<String> ls = new ArrayList<String>();
			Matcher m = Pattern.compile(key).matcher(str);
			while(m.find())ls.add(m.group(1));
			return ls;
		}
		public static final String merge(String[] stra){
			StringBuilder sb = new StringBuilder();
			for(String str : stra)sb.append(str);
			return sb.toString();
		}
		public static final String merge(String[] stra, String separator){
			StringBuilder sb = new StringBuilder();
			int len = stra.length - 1;
			for(int i = 0; i < len; i++)sb.append(stra[i] + separator);
			return sb.append(stra[len]).toString();
		}
		public static final String toString(String utfStr){
			StringBuilder sb = new StringBuilder();
			int i = -1, index = 0;
			while((i = utfStr.indexOf("\\u", index)) != -1){
				sb.append(utfStr.substring(index, i));
				if((index = i + 6) < utfStr.length() + 1)sb.append((char) Integer.parseInt(utfStr.substring(i + 2, index), 16));
				else index -= 6;
			}
			return sb.append(utfStr.substring(index, utfStr.length())).toString();
		}
		public static final String toUnicode(String srcStr){
			StringBuilder sb = new StringBuilder();
			for(char c : srcStr.toCharArray()){
				sb.append("\\u")
				.append(Oneself.execute(Integer.toHexString(c >> 0x8), s -> {if(s.length() == 1)sb.append("0");}))
				.append(Oneself.execute(Integer.toHexString(c & 0xFF), s -> {if(s.length() == 1)sb.append("0");}));
			}
			return sb.toString();
		}
		public static final String getMessageDigest(String srcStr, String encode, int... k){
			try {
				StringBuilder sb = new StringBuilder();
				for(int i : Oneself.execute(MessageDigest.getInstance(encode), m -> {
					try{m.update(srcStr.getBytes("UTF-8"));}
					catch(Exception e){e.printStackTrace();}
				}).digest()){
					if(i < 0)i += 256;
					if(i < 16)sb.append("0");
					sb.append(Integer.toHexString(i));
				}
				return k.length > 0 && k[0] > 0 ? getMessageDigest(sb.toString(), encode, k[0] - 1) : sb.toString();
			} catch(Exception e){return null;}
		}
		public static final String getSha_1(String srcStr, int... k){
			return getMessageDigest(srcStr, "SHA-1", k);
		}
		public static final String getMD5_32(String srcStr, int... k){
			return getMessageDigest(srcStr, "MD5", k);
		}
		public static final String getMD5_16(String srcStr, int... k){
			return getMD5_32(srcStr, k).substring(8, 24);
		}
		public static final boolean isBlank(char c){
			return c == '\n' || c == '\t' || c == ' ';
		}
		public static final boolean isBlank(String str){
			for(char c : str.toCharArray())
				if(!isBlank(c))return false;
			return true;
		}
		public static final StringBuilder replace(StringBuilder sb, String src, String to){
			int index = 0, src_len = src.length(), to_len = to.length();
			while((index = sb.indexOf(src, index + to_len)) != -1)sb.replace(index, index + src_len, to);
			return sb;
		}
		public static final StringBuffer replace(StringBuffer sb, String src, String to){
			int index = 0, src_len = src.length(), to_len = to.length();
			while((index = sb.indexOf(src, index + to_len)) != -1)sb.replace(index, index + src_len, to);
			return sb;
		}
		public static final String getBlank(int num){
			return getBlank(num, ' ');
		}
		public static final String getBlank(int num, char blank){
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < num; i++)sb.append(blank);
			return sb.toString();
		}
	}
	public static final class Files {
		public static final String fixName(String name){
			return name.replaceAll("[\\\\/:*?\"<>|]", "_");
		}
		public static final String name(String path){
			return Strings.get(path, ".*[\\\\/](.*)\\..*");
		}
		public static final String type(String path){
			return Strings.get(path, ".*\\.(.*)");
		}
		public static final String completeName(String path){
			return Strings.get(path, ".*[\\\\/](.*)");
		}
		public static final String toStdPath(String path){
			return path.replaceAll("[\\\\/]{1,}", "/");
		}
		public static final String toStdDirPath(String path){
			String result = toStdPath(path);
			return result.endsWith("/") ? result : result + "/";
		}
		public static final boolean isType(String name, String type){
			return name.substring(name.length() - type.length()).toLowerCase().equals(type) ||
					name.substring(name.length() - type.length()).toUpperCase().equals(type);
		}
		public static final boolean checkDir(String path){
			File file = new File(path);
			return file.isDirectory() || file.mkdirs();
		}
		public static final boolean exists(String path){
			return new File(path).exists();
		}
		public static final String getParent(String path){
			return new File(path).getParent();
		}
		public static final List<String> getAll(String path){
			return Oneself.execute(new ArrayList<String>(), s -> getAll(new File(path), s));
		}
		public static final void getAll(File f, List<String> s){
			if(f.exists())if(f.isDirectory()){
				File fa[] = f.listFiles();
				if(fa == null)return;
				for(File ft : fa)getAll(ft, s);
			} else s.add(f.getPath());
		}
		public static final long size(String path){
			try(FileInputStream fin = new FileInputStream(path);
				FileChannel fc = fin.getChannel()){
				return fc.size();
			} catch(Exception e){return -1;}
		}
		public static final void deleteAllEmptyDir(String path){
			deleteAllEmptyDir(new File(path));
		}
		public static final void deleteAllEmptyDir(File f){
			if(f.exists())if(f.isDirectory()){
				File fa[] = f.listFiles();
				if(fa == null)return;
				if(fa.length == 0){
					f.delete();
					return;
				}
				for(File ft : fa)deleteAllEmptyDir(ft);
			}
		}
		public static final void deleteAll(String path){
			deleteAll(new File(path));
		}
		public static final void deleteAll(File f){
			if(f.exists())if(f.isDirectory()){
				File fa[] = f.listFiles();
				if(fa == null)return;
				for(File ft : fa)deleteAll(ft);
				f.delete();
			} else f.delete();
		}
		public static final boolean deleteFile(String path, String name){
			return deleteFile(path + File.separator + name);
		}
		public static final boolean deleteFile(String path){
			File file = new File(path);
			return file.exists() && file.isFile() && file.delete();
		}
		public static final boolean renameFile(String path, String oldName, String newName){
			File oldFile = new File(path + File.separator + oldName);
			File newFile = new File(path + File.separator + newName);
			return oldFile.exists() && !newFile.exists() && oldFile.renameTo(newFile);
		}
		public static final boolean renameDir(String oldName, String newName){
			File oldFile = new File(oldName);
			File newFile = new File(newName);
			return oldFile.exists() && !newFile.exists() && oldFile.renameTo(newFile);
		}
		public static final boolean copy(String src, String to){
			try(FileInputStream fin = new FileInputStream(src);
				FileOutputStream fout = new FileOutputStream(to);
				FileChannel in = fin.getChannel();
				FileChannel out = fout.getChannel()){
				in.transferTo(0, in.size(), out);
				return true;
			} catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		public static final boolean copyDir(String src, String to){
			String src_std = toStdDirPath(src), to_std = toStdDirPath(to);
			Container<Boolean> c_bool = new Container<Boolean>(Boolean.TRUE);
			Tool.Files.getAll(src).stream().map(s -> toStdPath(s)).peek(s -> checkDir(getParent(to_std + s.replace(src_std, ""))))
				.forEach(s -> c_bool.i = copy(s, to_std + s.replace(src_std, "")));
			return c_bool.i;
		}
		public static final boolean remove(String src, String to){
			return copy(src, to) && deleteFile(src);
		}
		public static final String getMessageDigest(String path, String encode, int... k){
			try {
				StringBuilder sb = new StringBuilder();
				for(int i : Oneself.execute(MessageDigest.getInstance(encode), m -> m.update(getByteBuffer(path))).digest()){
					if(i < 0)i += 256;
					if(i < 16)sb.append("0");
					sb.append(Integer.toHexString(i));
				}
				return k.length > 0 && k[0] > 0 ? getMessageDigest(sb.toString(), encode, k[0] - 1) : sb.toString();
			} catch(Exception e){return null;}
		}
		public static final String getSha_1(String path, int... k){
			return getMessageDigest(path, "SHA-1", k);
		}
		public static final String getMD5_32(String path, int... k){
			return getMessageDigest(path, "MD5", k);
		}
		public static final String getMD5_16(String path, int... k){
			return getMD5_32(path, k).substring(8, 24);
		}
		public static final MappedByteBuffer getByteBuffer(String path){
			try(FileInputStream fin = new FileInputStream(path)){
				return fin.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, size(path));
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		public static byte[] toByteArray(String path){
			try(FileChannel fc = new RandomAccessFile(path, "r").getChannel()){
				MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size()).load();
				byte[] result = new byte[(int) fc.size()];
				if(byteBuffer.remaining() > 0)byteBuffer.get(result, 0, byteBuffer.remaining());
				return result;
			} catch(Exception e){
				return new byte[0];
			}
		}
		public static String getSizeString(long size) {
			if (size < 1024)
				return size + " B";
			else if ((size /= 1024) < 1024)
				return size + " KB";
			else if ((size /= 1024) < 1024)
				return size + " MB";
			else if ((size /= 1024) < 1024)
				return size + " GB";
			else if ((size /= 1024) < 1024)
				return size + "TB";
			return size + " B";
		}
	}
	public static final class Objects {
		private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING = new HashMap<>();
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
		public static final Class<?> getPrimitiveMapping(Class<?> clazz){
			return PRIMITIVE_MAPPING.get(clazz);
		}
		private static final Class<?>[] SIMPLE = new Class[]{Character.class, String.class, Boolean.class,
				Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};
		public static final boolean isPacking(Class<?> clazz){
			return Arrays.contains(SIMPLE, clazz);
		}
		public static final boolean isSimple(Class<?> clazz){
			return clazz.isPrimitive() || Arrays.contains(SIMPLE, clazz);
		}
		public static final boolean isBasics(Class<?> clazz){
			return isSimple(clazz) || clazz == String.class;
		}
		public static final boolean nonNull(Object... obj){
			for(Object o : obj)if(o == null)return false;
			return true;
		}
		public static final <T> void nonNullDoConsumer(T t, Consumer<T> c){
			if(t != null)c.accept(t);
		}
		public static final <T, R> R nonNullDoFunction(T t, Function<T,R> f){
			if(t != null)return f.apply(t);
			else return null;
		}
		public static final <T> T isNullOr(T t, T or){
			return t == null ? or : t;
		}
		@SafeVarargs public static final <T> T isNullOr(T... ta){
			for(T t : ta)if(t != null)return t;
			return null;
		}
		public static final boolean equals(Object a, Object b){
			return a == null ? b == null : a.equals(b);
		}
		public static final void notify(Object... obj){
			for(Object o : obj)synchronized(o){o.notify();}
		}
		public static final void notifyAll(Object... obj){
			for(Object o : obj)synchronized(o){o.notifyAll();}
		}
		public static final <T> T read(String path, String name){
			return read(path + File.separatorChar + name);
		}
		@SuppressWarnings("unchecked")
		public static final <T> T read(String path){
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))){return (T) ois.readObject();}
			catch(Exception e){return null;}
		}
		public static final boolean save(String path, String name, Object obj){
			return save(path + File.separatorChar + name, obj);
		}
		public static final boolean save(String path, Object obj){
			Files.checkDir(new File(path).getParent());
			try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))){
				oos.writeObject(obj);
				return true;
			} catch(Exception e){return false;}
		}
		public static final Object readFromByteArray(byte[] buf){
			try(ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				ObjectInputStream ois = new ObjectInputStream(bais)){
				return ois.readObject();
			} catch(Exception e){return null;}
		}
	}
	public static final class Collections {
		@SuppressWarnings("unchecked")
		public static final <T> Collection<T> copy(Collection<T> c) {
			Collection<T> result;
			try {
				result = c.getClass().newInstance();
				c.forEach(t -> result.add(t));
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		public static final <T> Collection<T> copyToList(Collection<T> c){
			return c.stream().collect(Collectors.toList());
		}
	}
	public static final class Arrays {
		public static final int LOWERCASE_LETTERS = 0b0001, UPPERCASE_LETTERS = 0b0010, LETTERS = 0b0011,
				NUMBER = 0b0100, LETTERS_NUMBER = 0b0111, SYMBOL = 0b1000, ALL = 0b1111;
		public static final char[] getCharacter(int need){
			char[] c = new char[((need & LOWERCASE_LETTERS) > 0 ? 26 : 0) + ((need & UPPERCASE_LETTERS) > 0 ? 26 : 0) +
					((need & NUMBER) > 0 ? 10 : 0) + ((need & SYMBOL) > 0 ? 15 : 0)];
			int index = 0;
			if((need & LOWERCASE_LETTERS) > 0){
				for(int i = 0; i < 26; i++)c[i + index] = (char) (97 + i);
				index += 26;
			}
			if((need & UPPERCASE_LETTERS) > 0){
				for(int i = 0; i < 26; i++)c[i + index] = (char) (65 + i);
				index += 26;
			}
			if((need & NUMBER) > 0){
				for(int i = 0; i < 10; i++)c[i + index] = (char) (48 + i);
				index += 10;
			}
			if((need & SYMBOL) > 0){
				for(int i = 0; i < 15; i++)c[i + index] = (char) (33 + i);
				index += 15;
			}
			return c;
		}
		public static final <T> boolean contains(T[] tarr, T t){
			for(T tt : tarr)if(tt.equals(t))return true;
			return false;
		}
		public static final <T> boolean equals(T[] arr1, T[] arr2){
			if(arr1.length != arr2.length)return false;
			for(int i = 0; i< arr1.length; i++)if(!arr1[i].equals(arr2[i]))return false;
			return true;
		}
		public static <T> T[] merge(T[] tarr1, T[] tarr2){
			T[] result = copyOf(tarr1, tarr1.length + tarr2.length);
			System.arraycopy(tarr2, 0, result, tarr1.length, tarr2.length);
			return result;
		}
		@SuppressWarnings("unchecked")
		@SafeVarargs public static <T> T[] merge(T[]... taarr){
			int index = 0;
			long len = 0;
			for(T[] tarr : taarr)len += tarr.length;
			T[] result = (T[]) getGenericArray(taarr.getClass().getComponentType().getComponentType(), (int) len);
			for(T[] tarr : taarr){
				System.arraycopy(tarr, 0, result, index, tarr.length);
				index += tarr.length;
			}
			return result;
		}
		@SuppressWarnings("unchecked")
		public static final <T> T[][] splitArgs(T[] tarr, T[] targs){
			if(targs == null)return (T[][]) (getGenericArray((Class<T[]>) tarr.getClass(), 1)[0] = tarr);
			List<Integer> li = new LinkedList<Integer>();
			int index = 0;
			for(T t : tarr){
				if(contains(targs, t))li.add(index);
				index++;
			}
			T[][] result = getGenericArray((Class<T[]>) tarr.getClass(), li.size());
			Class<T> type = (Class<T>) tarr.getClass().getComponentType();
			int last = 0, next = 0;
			for(int i : li){
				int length = --i - last;
				result[next] = getGenericArray(type, length);
				System.arraycopy(tarr, last, result[next++], 0, length);
				last = i;
			}
			return result;
		}
		@SuppressWarnings("unchecked")
		public static final <T> T[][] split(T[] tarr, T[] regex){
			if(regex == null)return (T[][]) (getGenericArray((Class<T[]>) tarr.getClass(), 1)[0] = tarr);
			List<Integer> li = new LinkedList<Integer>();
			int num = 0, index = 0;
			for(T t : tarr){
				if(num == 0){
					if(t.equals(regex[0]))num = 1;
				} else {
					if(num < regex.length && t.equals(regex[num]))if(num == regex.length - 1){
						li.add(index + 1);
						num = 0;
					} else num++;
				}
				index++;
			}
			T[][] result = getGenericArray((Class<T[]>) tarr.getClass(), li.size());
			Class<T> type = (Class<T>) tarr.getClass().getComponentType();
			int last = 0, next = 0;
			for(int i : li){
				int length = i - regex.length - last;
				result[next] = getGenericArray(type, length);
				System.arraycopy(tarr, last, result[next++], 0, length);
				last = i;
			}
			return result;
		}
		@SuppressWarnings("unchecked")
		public static final <T> T[] getGenericArray(Class<T> clazz, int length){
			return (T[]) Array.newInstance(clazz, length);
		}
		public static final <T> Object getMultidimensionalGenericArray(Class<T> clazz, int length, int dimensions){
			Class<?> t = clazz;
			for(int i = 0; i < dimensions - 1; i++)t = getGenericArray(t, 0).getClass();
			return getGenericArray(clazz, length);
		}
		@SuppressWarnings("unchecked")
		public static final <T> T[] reverse(T[] ta){
			T[] result = (T[]) getGenericArray(ta.getClass().getComponentType(), ta.length);
			int index = result.length;
			for(T t : ta)result[--index] = t;
			return result;
		}
		public static final byte[] reverse(byte[] ba) {
			byte[] result = new byte[ba.length];
			int index = result.length;
			for(byte b : ba)result[--index] = b;
			return result;
		}
		public static final <T> boolean isRange(int index, T[] t){
			return index > -1 && index < t.length;
		}
		public static final BiConsumer<Container<Integer>, String> execute(Object arr){
			switch(arr.getClass().getComponentType().getName()){
				case "boolean":return (i, s) -> Array.setBoolean(arr, i.i++, Boolean.valueOf(s));
				case "byte":return (i, s) -> Array.setByte(arr, i.i++, Byte.valueOf(s));
				case "short":return (i, s) -> Array.setShort(arr, i.i++, Short.valueOf(s));
				case "int":return (i, s) -> Array.setInt(arr, i.i++, Integer.valueOf(s));
				case "long":return (i, s) -> Array.setLong(arr, i.i++, Long.valueOf(s));
				case "float":return (i, s) -> Array.setFloat(arr, i.i++, Float.valueOf(s));
				case "double":return (i, s) -> Array.setDouble(arr, i.i++, Double.valueOf(s));
				case "char":return (i, s) -> Array.setChar(arr, i.i++, s.charAt(0));
			}
			return null;
		}
	}
	public static final class Calculate {
		public static final int length(long l){
			return length(l, 10);
		}
		public static final int length(long l, int hex){
			int i = 1;
			while((l /= hex) > 0)i++;
			return i;
		}
		public static final List<Integer> split(long l){
			List<Integer> li = new LinkedList<Integer>();
			do li.add((int) (l % 10));while((l /= 10) > 0);
			return li;
		}
		public static final byte[] intToByteArray(int i){
			return new byte[]{(byte) (i), (byte) (i >> 8), (byte) (i >> 16), (byte) (i >> 24)};
		}
		public static final int byteArrayToInt(byte[] b, int index){
			return (b[index + 3] & 0xFF) << 24 | (b[index + 2] & 0xFF) << 16 | (b[index + 1] & 0xFF) << 8 | b[index] & 0xFF;
		}
		public static final int byteArrayToInt(byte[] b){
			return byteArrayToInt(b, 0);
		}
	}
	public static final class Streams {
		public static final PrintStream outputPacking(OutputStream out, final Consumer<String> c){
			return new PrintStream(out){
				@Override public void write(byte[] buf, int off, int len) {
					String message = new String(buf, off, len);
					super.write(buf, off, len);
					c.accept(message);
				}
			};
		}
		public static synchronized final void systemOutPacking(Consumer<String> c){
			System.setOut(outputPacking(System.out, c));
		}
		public static synchronized final void systemErrPacking(Consumer<String> c){
			System.setErr(outputPacking(System.err, c));
		}
		public static final void systemPutPacking(Consumer<String> c){
			systemOutPacking(c);
			systemErrPacking(c);
		}
	}
	public static final class Numbers {
		public static final Integer parseInt(String str){
			if(str.startsWith("0x"))return Integer.parseInt(str.substring(2), 16);
			else if(str.startsWith("0b"))return Integer.parseInt(str.substring(2), 2);
			else if(str.startsWith("0"))return Integer.parseInt(str.substring(1), 8);
			else return Integer.parseInt(str);
		}
		public static final void add(Pointer<?> p, Number num){
			Object obj = p.get();
			Class<?> c = Objects.isNullOr(Objects.getPrimitiveMapping(p.getType()), p.getType());
			if(c == Integer.class)p.set((int) obj +num.intValue());
			else if(c == Long.class)p.set((long) obj + num.longValue());
			else if(c == Double.class)p.set((double) obj + num.doubleValue());
			else if(c == Float.class)p.set((float) obj + num.floatValue());
			else if(c == Short.class)p.set((short) obj + num.shortValue());
			else if(c == Byte.class)p.set((byte) obj + num.byteValue());
			else if(c == Character.class)p.set((char) obj + num.byteValue());
		}
		public static final void set(Pointer<?> p, Number num){
			Class<?> c = Objects.isNullOr(Objects.getPrimitiveMapping(p.getType()), p.getType());
			if(c == Integer.class)p.set(num.intValue());
			else if(c == Long.class)p.set(num.longValue());
			else if(c == Double.class)p.set(num.doubleValue());
			else if(c == Float.class)p.set(num.floatValue());
			else if(c == Short.class)p.set(num.shortValue());
			else if(c == Byte.class)p.set(num.byteValue());
			else if(c == Character.class)p.set(num.intValue());
		}
		public static final Object get(Class<?> c, Number num){
			c = Objects.isNullOr(Objects.getPrimitiveMapping(c), c);
			if(c == Integer.class)return num.intValue();
			else if(c == Long.class)return num.longValue();
			else if(c == Double.class)return num.doubleValue();
			else if(c == Float.class)return num.floatValue();
			else if(c == Short.class)return num.shortValue();
			else if(c == Byte.class)return num.byteValue();
			else if(c == Character.class)return num.intValue();
			return null;
		}
	}
	public static final class Zips {
		public static final void fromDir(String srcPath, String toPath){
			Files.checkDir(Files.getParent(toPath));
			try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(toPath))){
				Files.getAll(srcPath).forEach(s -> {
					try(FileInputStream fis = new FileInputStream(s)){
						zos.putNextEntry(new ZipEntry(s.substring(srcPath.length())));
						byte[] buf = new byte[1024 * 1024];
						for(int length = 0; (length = fis.read(buf)) != -1;)zos.write(buf, 0, length);
					} catch(Exception e){e.printStackTrace();}
				});
				zos.finish();
			} catch(Exception e){e.printStackTrace();}
		}
		public static final void toDir(String srcPath, String toPath){
			Files.checkDir(toPath);
			try(ZipInputStream zis = new ZipInputStream(new FileInputStream(srcPath))){
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null)
					if(entry.isDirectory())Files.checkDir(toPath + entry.getName());
					else try(FileOutputStream fos = new FileOutputStream(toPath + entry.getName())){
						byte[] buf = new byte[1024 * 1024];
						for(int length = 0; (length = zis.read(buf)) != -1;)fos.write(buf, 0, length);
					} catch(Exception e){e.printStackTrace();}
			} catch(Exception e){e.printStackTrace();}
		}
	}
}
