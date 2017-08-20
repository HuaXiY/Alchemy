package index.alchemy.util;

import index.project.version.annotation.Omega;

@Omega
public class DynamicNumber<T extends Number> extends Number {
	
	public T value;
	
	public DynamicNumber(T number) {
		value = number;
	}
	
	@Override
	public byte byteValue() {
		return value.byteValue();
	}
	
	@Override
	public short shortValue() {
		return value.shortValue();
	}
	
	@Override
	public int intValue() {
		return value.intValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public float floatValue() {
		return value.floatValue();
	}

	@Override
	public double doubleValue() {
		return value.doubleValue();
	}
	
	public boolean bigger(T t) {
		if (value instanceof Long)
			return value.longValue() > t.longValue();
		else
			return value.doubleValue() > t.doubleValue();
	}
	
	public boolean biggerOrEquals(T t) {
		if (value instanceof Long)
			return value.longValue() >= t.longValue();
		else
			return value.doubleValue() >= t.doubleValue();
	}
	
	public boolean smaller(T t) {
		if (value instanceof Long)
			return value.longValue() < t.longValue();
		else
			return value.doubleValue() < t.doubleValue();
	}
	
	public boolean smallerOrEquals(T t) {
		if (value instanceof Long)
			return value.longValue() <= t.longValue();
		else
			return value.doubleValue() <= t.doubleValue();
	}
	
	public T update(T t) {
		T result = value;
		set(t);
		return result;
	}
	
	public void set(T t) {
		value = t;
	}
	
	public T get() {
		return value;
	}

}
