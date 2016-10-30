package mapi.xcore;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public interface Referenceable<T> extends Serializable  {
	public default Class getDeclaringClass(){
		return null;
	}
	public Class getType();
	public T get();
	public void set(Object o);
}
