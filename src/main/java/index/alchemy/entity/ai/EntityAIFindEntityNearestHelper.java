package index.alchemy.entity.ai;

import java.util.Comparator;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;

@Omega
public class EntityAIFindEntityNearestHelper {
	
	public static class Sorter implements Comparator<Entity> {
		
		private final Entity entity;

		public Sorter(Entity entity) {
			this.entity = entity;
		}

		public int compare(Entity a, Entity b) {
			double d0 = this.entity.getDistanceSqToEntity(a);
			double d1 = this.entity.getDistanceSqToEntity(b);
			return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
		}
		
	}
	
	public static boolean isSuitableLivingTarget(EntityLivingBase attacker, EntityLivingBase target) {
		return attacker instanceof EntityLiving ?
				EntityAINearestAttackableTarget.isSuitableTarget((EntityLiving) attacker, target, false, true) : true;
	}
	
	public static AxisAlignedBB getTargetRange(EntityLivingBase source, double d) {
		return source.getEntityBoundingBox().expand(d, 4, d);
	}
	
	public static double getTargetDistance(EntityLivingBase source) {
		IAttributeInstance attr = source.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
		return attr == null ? 16 : attr.getAttributeValue();
	}
	
	@Nullable
	public static <T extends EntityLivingBase> T findNearest(EntityLivingBase source, Class<T> type, @Nullable AxisAlignedBB aabb,
			@Nullable Predicate<T> req) {
		return source.world.getEntitiesWithinAABB(type, Tool.isNullOr(aabb, () -> getTargetRange(source, getTargetDistance(source))))
				.stream().filter(Tool.isNullOr(req, () -> l -> true)).sorted(new Sorter(source)).findFirst().orElse(null);
	}

}
