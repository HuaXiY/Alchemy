package index.alchemy.entity;

import biomesoplenty.common.entities.EntityButterfly;
import index.alchemy.api.IFollower;
import index.alchemy.api.annotation.EntityMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.util.math.MathHelper.*;

import java.util.UUID;

import com.google.common.base.Optional;

@EntityMapping("magic_butterfly")
public class EntityMagicButterfly extends EntityButterfly implements IFollower {
	
	public static final DataParameter<Optional<UUID>> owner_uuid = EntityDataManager.<Optional<UUID>>createKey(EntityMagicButterfly.class,
			DataSerializers.OPTIONAL_UNIQUE_ID);
	
	protected EntityLivingBase owner;
	protected boolean projectionState;
	@SideOnly(Side.CLIENT)
	protected transient MovementInput movementInput = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);

	public EntityMagicButterfly(World world) {
		super(world);
		tasks.taskEntries.clear();
		dataManager.register(owner_uuid, Optional.fromNullable(null));
	}
	
	@Override
	public void setOwner(EntityLivingBase owner) {
		this.owner = owner;
		dataManager.set(owner_uuid, Optional.of(owner.getUniqueID()));
	}
	
	@Override
	public EntityLivingBase getOwner() { return owner; }
	
	@Override
	public void setProjectionState(boolean projectionState) { this.projectionState = projectionState; }

	@Override
	public boolean getProjectionState() { return projectionState; }
	
	@Override
	public boolean isEntityInvulnerable(DamageSource source) { return true; }
	
	@Override
	protected void updateAITasks() {
		if (owner == null) {
			setDead();
			return;
		}
		if (ticksExisted < 20) {
			ticksExisted = 20;
			playLivingSound();
		}
		livingSoundTime = 0;
		if (rand.nextInt(450) == 0)
			playLivingSound();
		if (getProjectionState())
			return;
		float sin = sin(owner.rotationYaw * 0.017453292F);
        float cos = cos(owner.rotationYaw * 0.017453292F);
		double x = -cos + sin + owner.posX + rand.nextGaussian() * 0.4D - 0.2D - posX;
        double y = owner.posY + rand.nextGaussian() * 0.2D + owner.height + 0.2D - posY;
        double z = -cos - sin + owner.posZ + rand.nextGaussian() * 0.4D - 0.2D - posZ;
        motionX += (Math.signum(x) * 0.5D - motionX) * 0.2D;
        motionY += (Math.signum(y) * 0.7D - motionY) * 0.2D;
        motionZ += (Math.signum(z) * 0.5D - motionZ) * 0.2D;
        moveForward = 0.5F;
        float atan = (float) (MathHelper.atan2(motionZ, motionX) * (180D / Math.PI)) - 90.0F;
        rotationYaw += MathHelper.wrapDegrees(atan - rotationYaw);
    }
	
	@SideOnly(Side.CLIENT)
	public void bindUUID(UUID uuid) {
		for (Entity entity : Minecraft.getMinecraft().theWorld.getLoadedEntityList())
			if (entity instanceof EntityLivingBase && entity.getUniqueID().equals(uuid)) {
				owner = (EntityLivingBase) entity;
				IFollower.follower.set(owner, this);
			}
	}

}
