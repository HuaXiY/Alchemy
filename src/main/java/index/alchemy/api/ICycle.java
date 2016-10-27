package index.alchemy.api;

public interface ICycle {
	
	float next();
	
	float update(int tick);
	
	ICycle copy();
	
}