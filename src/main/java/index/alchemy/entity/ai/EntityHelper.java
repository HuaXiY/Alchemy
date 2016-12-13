package index.alchemy.entity.ai;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

public class EntityHelper {
	
	public static <T extends Entity> T clone(T entity) {
		T result = (T) EntityList.createEntityFromNBT(entity.serializeNBT(), entity.worldObj);
		result.setUniqueId(UUID.randomUUID());
		return result;
	}
	
	public static <T extends Entity> T respawn(T entity) {
		T result = clone(entity);
		entity.setDead();
		return result;
	}
	
}
