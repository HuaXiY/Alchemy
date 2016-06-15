package index.alchemy.entity.ai;

import index.alchemy.annotation.Init;
import index.alchemy.annotation.Test;
import index.alchemy.api.Alway;
import index.alchemy.api.ILocationProvider;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.VectorHelper;
import net.minecraft.entity.Entity;import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import static java.lang.Math.*;

public class EntityTrackTracker {
	
	private ILocationProvider location;
	private double acceleration, deceleration_distance;
	
	public EntityTrackTracker(ILocationProvider location, double acceleration) {
		this.location = location;
		this.acceleration = acceleration;
		//this.maximum_speed = maximum_speed;
	}
	
	public void update(Entity tracker, double offsetY) {
		Vec3d pos = location.getLocation();
		Vec3d ac = VectorHelper.offset(pos, tracker.getPositionVector().addVector(0, offsetY, 0));
		
		double ax = ac.xCoord * acceleration;
		double ay = ac.yCoord * acceleration;
		double az = ac.zCoord * acceleration;

		/*boolean flagX = tracker.posX - pos.xCoord > 0;
		boolean flagY = tracker.posY - pos.yCoord > 0;
		boolean flagZ = tracker.posZ - pos.zCoord > 0;
		
		tracker.motionX = accelerate(tracker.motionX, acceleration, min(abs(tracker.posX - pos.xCoord) / deceleration_distance, 1) * maximum_speed, flagX);
		tracker.motionY = accelerate(tracker.motionY, acceleration, min(abs(tracker.posY - pos.yCoord) / deceleration_distance, 1) * maximum_speed, flagY);
		tracker.motionZ = accelerate(tracker.motionZ, acceleration, min(abs(tracker.posZ - pos.zCoord) / deceleration_distance, 1) * maximum_speed, flagZ);*/
		
		System.out.println(ac.xCoord + " - " + ac.yCoord + " - " + ac.zCoord);
		
		tracker.motionX += ax;
		tracker.motionY += ay;
		tracker.motionZ += az;
		
	}
	
	/*public static double vector(double amount, double t_pos, double pos, double deceleration_distance, double maximum_speed) {
		return deceleration_distance > 0 ? amount > 0 ? min(abs(t_pos - pos) / deceleration_distance, 1) : max(abs(t_pos - pos) / deceleration_distance, 1) : maximum_speed;
	}
	
	public static double accelerate(double src, double amount, double boundary, boolean flag) {
		return flag ? max(src - amount, -boundary) : min(src + amount, boundary);
	}*/
	
	@Test
	@Init(state = ModState.POSTINITIALIZED)
	public static class TEntityTrackTracker implements IPlayerTickable {
		
		private EntityTrackTracker test;
		
		@Override
		public Side getSide() {
			return null;
		}

		@Override
		public void onTick(EntityPlayer player, Phase phase) {
			/*if (test == null)
				test = new EntityTrackTracker(player);
			if (phase == Phase.START) {
				for (EntityLivingBase living : player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AABBHelper.getAABBFromEntity(player, 30D)))
					test.update(living);
			}*/
			for (EntityShulker shulker : player.worldObj.getEntitiesWithinAABB(EntityShulker.class, AABBHelper.getAABBFromEntity(player, 16)))
				System.out.println(shulker.getPeekTick());
			for (EntityArrow throwable : player.worldObj.getEntitiesWithinAABB(EntityArrow.class, AABBHelper.getAABBFromEntity(player, 2).addCoord(0, 1, 0))) {
				if (throwable.shootingEntity != null) {
					//new EntityTrackTracker(Alway.generateLocationProvider(throwable.shootingEntity, 1), 10).update(throwable, 0);
					new EntityTrackTracker(Alway.generateLocationProvider(player, 1.5), -1).update(throwable, 0);
				}
				//throwable.motionX *= -1;
				//throwable.motionY *= -1;
				//throwable.motionZ *= -1;
			}
		}
		
	}

}
