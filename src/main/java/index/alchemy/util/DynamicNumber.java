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

}
