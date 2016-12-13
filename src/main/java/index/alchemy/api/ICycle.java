package index.alchemy.api;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ICycle {
	
	float update(int tick);
	
	default float next() { return update(1); }
	
	@Nullable
	default ICycle copy() {
		try {
			return getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
}