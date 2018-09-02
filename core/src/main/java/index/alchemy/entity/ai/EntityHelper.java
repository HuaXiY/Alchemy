package index.alchemy.entity.ai;

import java.util.UUID;

import index.project.version.annotation.Omega;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

@Omega
public class EntityHelper {
    
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T clone(T entity) {
        T result = (T) EntityList.createEntityFromNBT(entity.serializeNBT(), entity.world);
        result.setUniqueId(UUID.randomUUID());
        return result;
    }
    
    public static <T extends Entity> T respawn(T entity) {
        T result = clone(entity);
        entity.setDead();
        return result;
    }
    
}
