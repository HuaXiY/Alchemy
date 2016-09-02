package index.alchemy.api;

public interface ICycle {
	
	public float next();
	
	public float update(int tick);
	
	public ICycle copy();
	
}