package index.alchemy.api;

public interface ISubElement<T> {
    
    T getParent();
    
    void setParent(T parent);
    
}
