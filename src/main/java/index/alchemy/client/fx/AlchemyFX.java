package index.alchemy.client.fx;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Always;
import index.alchemy.api.IFXUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AlchemyFX extends Particle {
	
	protected int brightness = -1;
	protected final List<IFXUpdate> update_list = new LinkedList<IFXUpdate>();
	
	protected AlchemyFX(World world, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
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
		update_list.addAll(updates);
		return (T) this;
	}
	
	public <T extends AlchemyFX> T removeFXUpdate(List<IFXUpdate> updates) {
		update_list.removeAll(updates);
		return (T) this;
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

	@Override
	public void onUpdate() {
		long tick = Always.getClientWorldTime();
		for (IFXUpdate update : update_list)
			update.updateFX(this, tick);
		super.onUpdate();
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	
	public int getBrightness() {
		return brightness;
	}
	
	@Override
	public int getBrightnessForRender(float partialTicks) {
		return brightness == -1 ? super.getBrightnessForRender(partialTicks) : brightness;
	}
	
	public static TextureAtlasSprite getAtlasSprite(String name) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
	}

}
