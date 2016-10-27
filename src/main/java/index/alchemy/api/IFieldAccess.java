package index.alchemy.api;

public interface IFieldAccess<O, T> {
	
	T get(O obj);
	
	void set(O obj, T t);
	
	default T get() { return get(null); }
	
	default void set(T t) { set(null, t); }

}
