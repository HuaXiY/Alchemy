package index.alchemy.util;

public class Pointer<V> {

	public V value;
	
	public V getValue() { return value; }
	
	public void setValue(V value) { this.value = value; }
	
	public Pointer() { }
	
	public Pointer(V value) {
		this.value = value;
	}
	
	public static final <V> Pointer<V> as(V value) { return new Pointer<>(value); }
	
}
