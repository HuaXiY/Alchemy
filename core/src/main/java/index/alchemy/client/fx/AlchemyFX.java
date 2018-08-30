package index.alchemy.client.fx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import index.alchemy.api.IFXUpdate;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;	
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@SideOnly(Side.CLIENT)
public abstract class AlchemyFX extends Particle {
	
	protected static final Random random = new Random();
	
	protected int brightness = -1;
	protected boolean transparent;
	protected Entity posSource;
	protected final List<IFXUpdate> update_list = Lists.newArrayList();
	
	protected AlchemyFX(World world, double vX, double vY, double vZ, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		motionX = vX;
		motionY = vY;
		motionZ = vZ;
	}
	
	@Override
	public void renderParticle(BufferBuilder worldRenderer, Entity entity, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (posSource != null) {
			float 
				posX = (float) (posSource.prevPosX + (posSource.posX - posSource.prevPosX) * partialTicks),
				posY = (float) (posSource.prevPosY + (posSource.posY - posSource.prevPosY) * partialTicks),
				posZ = (float) (posSource.prevPosZ + (posSource.posZ - posSource.prevPosZ) * partialTicks);
			this.posX += posX;
			this.posY += posY;
			this.posZ += posZ;
			this.prevPosX += posX;
			this.prevPosY += posY;
			this.prevPosZ += posZ;
			super.renderParticle(worldRenderer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
			this.posX -= posX;
			this.posY -= posY;
			this.posZ -= posZ;
			this.prevPosX -= posX;
			this.prevPosY -= posY;
			this.prevPosZ -= posZ;
		} else
			super.renderParticle(worldRenderer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AlchemyFX> T addFXUpdate(IFXUpdate... updates) {
		update_list.addAll(Arrays.asList(updates));
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AlchemyFX> T removeFXUpdate(IFXUpdate... updates) {
		update_list.removeAll(Arrays.asList(updates));
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AlchemyFX> T addFXUpdate(List<IFXUpdate> updates) {
		if (updates != null)
			update_list.addAll(updates);
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AlchemyFX> T removeFXUpdate(List<IFXUpdate> updates) {
		if (updates != null)
			update_list.removeAll(updates);
		return (T) this;
	}
	
	public void setPosX(double posX) {
		this.posX = posX;
	}
	
	public double getPosX() {
		return posX;
	}
	
	public void setPosY(double posY) {
		this.posY = posY;
	}
	
	public double getPosY() {
		return posY;
	}
	
	public void setPosZ(double posZ) {
		this.posZ = posZ;
	}
	
	public double getPosZ() {
		return posZ;
	}
	
	public void setMotionX(double motionX) {
		this.motionX = motionX;
	}
	
	public double getMotionX() {
		return motionX;
	}
	
	public void setMotionY(double motionY) {
		this.motionY = motionY;
	}
	
	public double getMotionY() {
		return motionY;
	}
	
	public void setMotionZ(double motionZ) {
		this.motionZ = motionZ;
	}
	
	public double getMotionZ() {
		return motionZ;
	}
	
	public void setScaleF(float scale) {
		particleScale = scale;
	}
	
	public float getScale() {
		return particleScale;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AlchemyFX> T update() {
		onUpdate();
		return (T) this;
	}

	@Override
	public void onUpdate() {
		long tick = Always.getClientWorldTime();
		for (Iterator<IFXUpdate> iterator = update_list.iterator(); iterator.hasNext();)
			if (iterator.next().updateFX(this, tick))
				iterator.remove();
		super.onUpdate();
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public void move(double x, double y, double z) {
		boundingBox = boundingBox.offset(x, y, z);
		resetPositionToBB();
	}
	
	public double getDistanceSq(Vec3d vec3d) {
		return getDistanceSq(vec3d.x, vec3d.y, vec3d.z);
	}
	
	public double getDistanceSq(double x, double y, double z) {
		double d0 = posX - x;
		double d1 = posY - y;
		double d2 = posZ - z;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	public double getDistanceSq(BlockPos pos) {
		return pos.distanceSq(posX, posY, posZ);
	}
	
	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	
	public int getBrightness() {
		return brightness;
	}
	
	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}
	
	public boolean isTransparent() {
		return transparent;
	}
	
	public void setPosSource(Entity posSource) {
		this.posSource = posSource;
	}
	
	public Entity getPosSource() {
		return posSource;
	}
	
	@Override
	public int getBrightnessForRender(float partialTicks) {
		return brightness == -1 ? super.getBrightnessForRender(partialTicks) : brightness;
	}
	
	public static TextureAtlasSprite getAtlasSprite(String name) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
	}

}
