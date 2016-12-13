package index.alchemy.api;

import net.minecraft.launchwrapper.IClassTransformer;

public interface IAlchemyClassTransformer extends IClassTransformer {
	
	String getTransformerClassName();
	
	default boolean disable() { return false; }
	
}
