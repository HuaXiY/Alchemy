package index.alchemy.core;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.Field;
import index.project.version.annotation.Alpha;
import net.minecraft.entity.Entity;

@Alpha
@Field.Provider
public class AlchemyFieldAccess {
	
	public static IFieldAccess<Entity, Integer> test;
	
	//public static IFieldAccess<? extends IProjectile, EntityLivingBase> owner;
	
}
