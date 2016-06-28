package index.alchemy.animation;

public interface ICycle {
	
	public float next();
	
	public float update(int tick);
	
	public ICycle copy();
	
}