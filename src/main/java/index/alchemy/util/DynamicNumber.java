package index.alchemy.util;

public class DynamicNumber<T extends Number> extends Number {
	
	public T value;
	
	public DynamicNumber(T number) {
		value = number;
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

}
