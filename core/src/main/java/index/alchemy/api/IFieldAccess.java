package index.alchemy.api;

public interface IFieldAccess<O, T> {
	
	T get(O owner);
	
	void set(O owner, T value);
	
	public interface Byte<O> {
			
		byte get(O owner);
			
		void set(O owner, byte value);
			
	}
	
	public interface Short<O> {
		
		short get(O owner);
		
		void set(O owner, short value);
		
	}
	
	public interface Int<O> {
		
		int get(O owner);
		
		void set(O owner, int value);
		
	}
	
	public interface Long<O> {
		
		long get(O owner);
		
		void set(O owner, long value);
		
}
	
	public interface Float<O> {
		
		float get(O owner);
		
		void set(O owner, float value);
		
	}
	
	public interface Double<O> {
		
		double get(O owner);
		
		void set(O owner, double value);
		
	}
	
	public interface Char<O> {
		
		char get(O owner);
		
		void set(O owner, char value);
		
	}
	
	public interface Boolean<O> {
		
		boolean get(O owner);
		
		void set(O owner, boolean value);
		
	}

}
