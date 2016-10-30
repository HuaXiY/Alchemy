package mapi.xcore;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Pointer<T> implements Referenceable<T> {
	private static final long serialVersionUID = -3590884291873984266L;
	private Class<?> type;
	private Object obj;
	private Field f;
	public final Supplier<T> get;
	public final Consumer<T> set;
	public Pointer(Supplier<T> get, Consumer<T> set){
		this.get = get;
		this.set = set;
		type = Object.class;
	}
	public Pointer(Supplier<T> get, Consumer<T> set, Class<?> type){
		this.get = get;
		this.set = set;
		this.type = type;
	}
	private Pointer(Object obj, String name) throws NoSuchFieldException {
		this(obj.getClass(), name);
		this.obj = obj;
	}
	private Pointer(Class<?> clazz, String name) throws NoSuchFieldException {
		if((f = Tool.Fields.getField(clazz, name)) == null)throw new NoSuchFieldException();
		type = f.getType();
		get = () -> {
			try {return (T) f.get(obj);}
			catch(Exception e){
				e.printStackTrace();
				return null;
			}
		};
		set = o -> {
			try {f.set(obj, o);}
			catch(Exception e){e.printStackTrace();}
		};
	}
	public static <T> Pointer<T> as(Object obj, String name){
		try {return new Pointer<>(obj, name);}
		catch(Exception e){
			throw new RuntimeException("The Field: " + name + ", " + "can't find in Class: " + obj.getClass().getName());
		}
	}
	public static Pointer[] as(Object obj, String... sa){
		Pointer[] pa = new Pointer[sa.length];
		int index = 0;
		try {
			for(String name : sa)pa[index++] = new Pointer<>(obj, name);
		} catch(NoSuchFieldException e){
			throw new RuntimeException("The Field: " + sa[index] + ", " + "can't find in Class: " + obj.getClass().getName());
		}
		return pa;
	}
	public static <T> Pointer<T> as(String caller, String name) throws ClassNotFoundException {
		try {return new Pointer<>(Class.forName(caller), name);}
		catch(NoSuchFieldException e){
			throw new RuntimeException("The Field: " + name + ", " + "can't find in Class: " + caller);
		}
	}
	public static Pointer[] as(String caller, String... sa) throws ClassNotFoundException {
		return as(Class.forName(caller), sa);
	}
	public static <T> Pointer<T> as(Class<?> caller, String name){
		try {return new Pointer<>(caller, name);}
		catch(NoSuchFieldException e){
			throw new RuntimeException("The Field: " + name + ", " + "can't find in Class: " + caller.getName());
		}
	}
	public static <T> Pointer[] as(Class<?> caller, String... sa){
		Pointer[] pa = new Pointer[sa.length];
		int index = 0;
		try {
			for(String name : sa)pa[index++] = new Pointer<>(caller, name);
		} catch(NoSuchFieldException e){
			throw new RuntimeException("The Field: " + sa[index] + ", " + "can't find in Class: " + caller.getName());
		}
		return pa;
	}
	@Override public Class getType(){
		return type;
	}
	@Override public Class getDeclaringClass(){
		return f.getDeclaringClass();
	}
	@Override public T get(){
		if(get != null)return get.get();
		else try {return (T) f.get(obj);}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	@Override public void set(Object o){
		if(set != null)set.accept((T) o);
		else try {f.set(obj, o);}
		catch(Exception e){e.printStackTrace();}
	}
}
