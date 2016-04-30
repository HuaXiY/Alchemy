package index.alchemy.entity.ai;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIFindEntityNearestHelper {
	
	public static AxisAlignedBB getTargetRange(EntityLiving source, double d) {
        return source.getEntityBoundingBox().expand(d, 4D, d);
    }
	
	public static double getTargetDistance(EntityLiving source) {
        IAttributeInstance attr = source.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return attr == null ? 16D : attr.getAttributeValue();
    }
	
	public static <T extends EntityLivingBase> T findNearest(final EntityLiving source, Class<T> type, final Predicate<T> req) {
		Predicate<T> targetEntitySelector = new Predicate<T>() {
	        public boolean apply(T living) {
	            return living != null && (req == null || req.apply(living)) && EntityAINearestAttackableTarget
	            		.isSuitableTarget(source, living, false, true);
	        }
	    };
	    List<T> list = source.worldObj.<T>getEntitiesWithinAABB(type,
	    		getTargetRange(source, getTargetDistance(source)), targetEntitySelector);
	    if (list.isEmpty()) 
	    	return null;
	    else 
	    	return list.get(0);
	}

}
