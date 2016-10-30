package mapi.xcore;

@FunctionalInterface
public interface Oneself<T> {
	public abstract void todo(T t);
	default T apply(T t){
		todo(t);
		return t;
	}
	public static <T> T execute(T t, Oneself<T> o){
		o.apply(t);
		return t;
	}
	@SafeVarargs public static <T> T execute(T t, Oneself<T>... o){
		for(Oneself<T> next : o)next.apply(t);
		return t;
	}
}
