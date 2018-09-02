package index.alchemy.util;

import index.alchemy.api.IFieldContainer;

public class FieldContainer<T> implements IFieldContainer<T> {
    
    protected T value;
    
    public FieldContainer() { }
    
    public FieldContainer(T value) {
        this.value = value;
    }
    
    @Override
    public T get() {
        return value;
    }
    
    @Override
    public void set(T value) {
        this.value = value;
    }
    
}
