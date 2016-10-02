package index.alchemy.entity.ai;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Predicate;

import index.alchemy.api.Time;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3i;

public class EntityAIEatMeat extends EntityAIBase implements Comparator<EntityItem> {
	
	protected static final String NBT_KEY_LAST_MEAL = "ai_last_meal";
	
	protected static final Predicate<EntityItem> IS_MEAT = new Predicate<EntityItem>() {
		
		@Override
		public boolean apply(EntityItem input) {
			return input.getEntityItem().getItem() instanceof ItemFood && ((ItemFood) input.getEntityItem().getItem()).isWolfsFavoriteMeat();
		}
		
	};
	
	protected EntityLiving living;
	protected EntityItem meat;
	
	public EntityAIEatMeat(EntityLiving living) {
		this.living = living;
		setMutexBits(1);
	}
	
	@Override
	public int compare(EntityItem o1, EntityItem o2) {
		return (int) (o1.getPosition().distanceSq(living.getPosition()) - o2.getPosition().distanceSq(living.getPosition()));
	}
	
	@Override
	public boolean shouldExecute() {
		if (living.getEntityData().getInteger(NBT_KEY_LAST_MEAL) > living.ticksExisted)
			living.getEntityData().setInteger(NBT_KEY_LAST_MEAL, -Time.DAY);
		PathNavigate navigate = living.getNavigator();
		if (living.getHealth() < living.getMaxHealth() ||
				living.ticksExisted - living.getEntityData().getInteger(NBT_KEY_LAST_MEAL) > 1) {
			List<EntityItem> list = living.worldObj.getEntitiesWithinAABB(EntityItem.class, AABBHelper.getAABBFromEntity(living, 32), IS_MEAT);
			list.sort(this);
			for (int i = list.size() - 1; i > -1; i--) {
				EntityItem item = list.get(i);
				navigate.tryMoveToEntityLiving(item, 1);
				Path path = navigate.getPath();
				if (path != null) {
					PathPoint point = path.getFinalPathPoint();
					if (item.getPosition().distanceSq(new Vec3i(point.xCoord, point.yCoord, point.zCoord)) < 2) {
						meat = item;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void updateTask() {
		double d;
		if (meat.isDead || (d = Math.pow(living.posX - meat.posX, 2) + Math.pow(living.posZ - meat.posZ, 2)) > 32 * 32) {
			resetTask();
			return;
		}
		if (meat.getPosition().distanceSq(living.getPosition()) < 2) {
			if (IS_MEAT.apply(meat)) {
				ItemStack food = meat.getEntityItem();
				if (--food.stackSize < 1)
					meat.setDead();
				living.heal(((ItemFood) food.getItem()).getHealAmount(food));
				living.getEntityData().setInteger(NBT_KEY_LAST_MEAL, living.ticksExisted);
				List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>();
				for (int i = 0; i < 7; i++)
					d6iaps.add(new Double6IntArrayPackage(
							living.posX + (living.rand.nextFloat() * living.width * 2.0F) - living.width,
							living.posY + 0.5D + living.rand.nextFloat() * living.height,
							living.posZ + (living.rand.nextFloat() * living.width * 2.0F) - living.width,
							living.rand.nextGaussian() * 0.02D, living.rand.nextGaussian() * 0.02D, living.rand.nextGaussian() * 0.02D));
				AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.HEART,
						AABBHelper.getAABBFromEntity(living, AlchemyNetworkHandler.getParticleRange()), living.worldObj, d6iaps);
			}
			resetTask();
		}
	}
	
	@Override
	public void resetTask() {
		meat = null;
	}

}
