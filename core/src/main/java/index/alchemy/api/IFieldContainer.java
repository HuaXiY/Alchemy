package index.alchemy.api;

public interface IFieldContainer<T> {
	
	T get();
	
	void set(T value);
	
	public interface Byte {
			
		byte get();
			
		void set(byte value);
			
	}
	
	public interface Short {
		
		short get();
		
		void set(short value);
		
	}
	
	public interface Int {
		
		int get();
		
		void set(int value);
		
	}
	
	public interface Long {
		
		long get();
		
		void set(long value);
		
}
	
	public interface Float {
		
		float get();
		
		void set(float value);
		
	}
	
	public interface Double {
		
		double get();
		
		void set(double value);
		
	}
	
	public interface Char {
		
		char get();
		
		void set(char value);
		
	}
	
	public interface Boolean {
		
		boolean get();
		
		void set(boolean value);
		
	}

}
