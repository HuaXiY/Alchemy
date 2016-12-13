package index.alchemy.api;

public interface IFieldAccess<O, T> {
	
	T get(O obj);
	
	void set(O obj, T t);
	
	default T get() { return get(null); }
	
	default void set(T t) { set(null, t); }
	
	public interface Byte<O> {
			
			byte get(O obj);
			
			void set(O obj, byte t);
			
			default byte get() { return get(null); }
			
			default void set(byte t) { set(null, t); }
			
	}
	
	public interface Short<O> {
		
		short get(O obj);
		
		void set(O obj, short t);
		
		default short get() { return get(null); }
		
		default void set(short t) { set(null, t); }
		
	}
	
	public interface Int<O> {
		
		int get(O obj);
		
		void set(O obj, int t);
		
		default int get() { return get(null); }
		
		default void set(int t) { set(null, t); }
		
	}
	
	public interface Long<O> {
		
		long get(O obj);
		
		void set(O obj, long t);
		
		default long get() { return get(null); }
		
		default void set(long t) { set(null, t); }
		
}
	
	public interface Float<O> {
		
		float get(O obj);
		
		void set(O obj, float t);
		
		default float get() { return get(null); }
		
		default void set(float t) { set(null, t); }
		
	}
	
	public interface Double<O> {
		
		double get(O obj);
		
		void set(O obj, double t);
		
		default double get() { return get(null); }
		
		default void set(double t) { set(null, t); }
		
	}
	
	public interface Char<O> {
		
		char get(O obj);
		
		void set(O obj, char t);
		
		default char get() { return get(null); }
		
		default void set(char t) { set(null, t); }
		
	}
	
	public interface Boolean<O> {
		
		boolean get(O obj);
		
		void set(O obj, boolean t);
		
		default boolean get() { return get(null); }
		
		default void set(boolean t) { set(null, t); }
		
	}

}
