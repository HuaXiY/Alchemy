package index.alchemy.api;

public interface ICastable<S> {
	
	@SuppressWarnings("unchecked")
	default <T extends S> T cast() { return (T) this; }

}
