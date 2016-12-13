package index.alchemy.client.fx;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.IFXUpdate;
import index.alchemy.util.Always;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AlchemyFX extends Particle {
	
	protected int brightness = -1;
	protected boolean transparent;
	protected final List<IFXUpdate> update_list = new LinkedList<IFXUpdate>();
	
	protected AlchemyFX(World world, double vX, double vY, double vZ, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		motionX = vX;
		motionY = vY;
		motionZ = vZ;
	}
	
	public <T extends AlchemyFX> T addFXUpdate(IFXUpdate... updates) {
		update_list.addAll(Arrays.asList(updates));
		return (T) this;
	}
	
	public <T extends AlchemyFX> T removeFXUpdate(IFXUpdate... updates) {
		update_list.removeAll(Arrays.asList(updates));
		return (T) this;
	}
	
	public <T extends AlchemyFX> T addFXUpdate(List<IFXUpdate> updates) {
		if (updates != null)
			update_list.addAll(updates);
		return (T) this;
	}
	
	public <T extends AlchemyFX> T removeFXUpdate(List<IFXUpdate> updates) {
		if (updates != null)
			update_list.removeAll(updates);
		return (T) this;
	}
	
	public double getPosX() {
		return posX;
	}
	
	public double getPosY() {
		return posY;
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
	public void moveEntity(double x, double y, double z) {
		setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
		resetPositionToBB();
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
	
	@Override
	public boolean isTransparent() {
		return transparent;
	}
	
	@Override
	public int getBrightnessForRender(float partialTicks) {
		return brightness == -1 ? super.getBrightnessForRender(partialTicks) : brightness;
	}
	
	public static TextureAtlasSprite getAtlasSprite(String name) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
	}

}
