package index.alchemy.entity;

import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.sin;

import index.alchemy.api.IFollower;
import index.alchemy.api.annotation.EntityMapping;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Patch;
import index.alchemy.client.render.RenderHelper;
import index.alchemy.util.Always;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Listener
@EntityMapping("mini_dragon")
public class EntityMiniDragon extends EntityDragon implements IFollower {
	
	@Hook.Provider
	@SideOnly(Side.CLIENT)
	@Patch("net.minecraft.client.renderer.entity.RenderDragon")
	public static class ExRenderDragon extends RenderDragon {

		@Patch.Exception
		public ExRenderDragon(RenderManager renderManager) { super(renderManager); }
		
		@Override
		@Patch.Spare
		protected void preRenderCallback(EntityDragon entitylivingbaseIn, float partialTicks) { }
		
		@Patch.Exception
		@Hook("net.minecraft.client.renderer.entity.RenderDragon#func_77041_b")
		public static Hook.Result preRenderCallback(RenderDragon render, EntityDragon dragon, float partialTicks) {
			if (dragon.getClass() == EntityMiniDragon.class) {
				RenderHelper.scale(0.1F, 0.1F, 0.1F);
				return Hook.Result.NULL;
			}
			return Hook.Result.VOID;
		}
		
	}
	
	protected EntityLivingBase owner;
	protected boolean projectionState;

	public EntityMiniDragon(World worldIn) {
		super(worldIn);
	}
	
	@Override
	public void onLivingUpdate() {
		prevAnimTime = animTime;
		float mxz = 0.2F / (MathHelper.sqrt(motionX * motionX + motionZ * motionZ) * 10.0F + 1.0F);
		mxz = mxz * (float) Math.pow(2.0D, motionY);
		animTime = mxz;
		rotationYaw = MathHelper.wrapDegrees(rotationYaw);
		
		if (ringBufferIndex < 0)
            for (int i = 0; i < ringBuffer.length; ++i) {
                ringBuffer[i][0] = (double)rotationYaw;
                ringBuffer[i][1] = posY;
            }

        if (++ringBufferIndex == ringBuffer.length)
            ringBufferIndex = 0;

        ringBuffer[ringBufferIndex][0] = (double)rotationYaw;
        ringBuffer[ringBufferIndex][1] = posY;
        
        if (Always.isClient())
        	if (newPosRotationIncrements > 0) {
                double nx = posX + (interpTargetX - posX) / newPosRotationIncrements;
                double ny = posY + (interpTargetY - posY) / newPosRotationIncrements;
                double nz = posZ + (interpTargetZ - posZ) / newPosRotationIncrements;
                double d2 = MathHelper.wrapDegrees(interpTargetYaw - rotationYaw);
                rotationYaw = (float) (rotationYaw + d2 / newPosRotationIncrements);
                rotationPitch = (float) (rotationPitch + (interpTargetPitch - rotationPitch) / newPosRotationIncrements);
                --newPosRotationIncrements;
                setPosition(nx, ny, nz);
                setRotation(rotationYaw, rotationPitch);
            }
        
        renderYawOffset = rotationYaw;
        dragonPartHead.width = 1.0F;
        dragonPartHead.height = 1.0F;
        dragonPartNeck.width = 3.0F;
        dragonPartNeck.height = 3.0F;
        dragonPartTail1.width = 2.0F;
        dragonPartTail1.height = 2.0F;
        dragonPartTail2.width = 2.0F;
        dragonPartTail2.height = 2.0F;
        dragonPartTail3.width = 2.0F;
        dragonPartTail3.height = 2.0F;
        dragonPartBody.height = 3.0F;
        dragonPartBody.width = 5.0F;
        dragonPartWing1.height = 2.0F;
        dragonPartWing1.width = 4.0F;
        dragonPartWing2.height = 3.0F;
        dragonPartWing2.width = 4.0F;
        
        double moa[] = getMovementOffsets(5, 1.0F);
        float moy = (float) (moa[1] - getMovementOffsets(10, 1.0F)[1]) * 10.0F * 0.017453292F;
        float cosoy = MathHelper.cos(moy);
        float sinoy = MathHelper.sin(moy);
        float ry = rotationYaw * 0.017453292F;
        float sinry = MathHelper.sin(ry);
        float cosry = MathHelper.cos(ry);
        dragonPartBody.onUpdate();
        dragonPartBody.setLocationAndAngles(posX + sinry * 0.5F, posY, posZ - cosry * 0.5F, 0.0F, 0.0F);
        dragonPartWing1.onUpdate();
        dragonPartWing1.setLocationAndAngles(posX + cosry * 4.5F, posY + 2.0D, posZ + sinry * 4.5F, 0.0F, 0.0F);
        dragonPartWing2.onUpdate();
        dragonPartWing2.setLocationAndAngles(posX - cosry * 4.5F, posY + 2.0D, posZ - sinry * 4.5F, 0.0F, 0.0F);
        
        float sinryryv = MathHelper.sin(rotationYaw * 0.017453292F - randomYawVelocity * 0.01F);
        float cosryryv = MathHelper.cos(rotationYaw * 0.017453292F - randomYawVelocity * 0.01F);
        dragonPartHead.onUpdate();
        dragonPartNeck.onUpdate();
        float f5 = getHeadYOffset();
        dragonPartHead.setLocationAndAngles(posX + sinryryv * 6.5F * cosoy, posY + f5 + sinoy * 6.5F, posZ - cosryryv * 6.5F * cosoy, 0.0F, 0.0F);
        dragonPartNeck.setLocationAndAngles(posX + sinryryv * 5.5F * cosoy, posY + f5 + sinoy * 5.5F, posZ - cosryryv * 5.5F * cosoy, 0.0F, 0.0F);

        for (int i = 0; i < 3; i++) {
        	MultiPartEntityPart part = null;
            if (i == 0)
            	part = dragonPartTail1;
            else if (i == 1)
            	part = dragonPartTail2;
            else if (i == 2)
            	part = dragonPartTail3;

            double[] nmoa = getMovementOffsets(12 + i * 2, 1.0F);
            float rymoryd = rotationYaw * 0.017453292F + (float) MathHelper.wrapDegrees(nmoa[0] - moa[0]) * 0.017453292F;
            float sinrymoryd = MathHelper.sin(rymoryd);
            float cosrymoryd = MathHelper.cos(rymoryd);
            float oi = (i + 1) * 2.0F;
            part.onUpdate();
            part.setLocationAndAngles(posX - (sinry * 1.5F + sinrymoryd * oi) * cosoy,
            		posY + (nmoa[1] - moa[1]) - (oi + 1.5F) * sinoy + 1.5D,
            		posZ + (cosry * 1.5F + cosrymoryd * oi) * cosoy, 0.0F, 0.0F);
        }
        
        //AlchemyEntity.onLivingUpdate(this);
	}
	
	protected float getHeadYOffset() {
        return (float) (getMovementOffsets(5, 1.0F)[1] - getMovementOffsets(0, 1.0F)[0]);
    }
	
	@Override
	public void setOwner(EntityLivingBase owner) { this.owner = owner; }
	
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
		if (owner == null)
			return;
		if (ticksExisted < 20) {
			ticksExisted = 20;
			playLivingSound();
		}
		livingSoundTime = 0;
		if (rand.nextInt(450) == 0)
			playLivingSound();
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

}
