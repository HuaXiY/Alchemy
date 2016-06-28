package index.alchemy.client.fx;

import static index.alchemy.client.color.ColorHelper.ahsbStep;

import java.awt.Color;
import java.util.Iterator;

import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.fx.update.FXUpdateHelper;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Texture({
	"alchemy:particle/wisp"
})
@FX(name = "wisp", factory = FXWisp.Factory.class, ignoreRange = false)
public class FXWisp extends AlchemyFX {
	
	private static final String TEXTURE_NAME[] = FXWisp.class.getAnnotation(Texture.class).value();
	
	public static final EnumParticleTypes type = null;
	
	private Iterator<Color> iterator = ahsbStep(new Color(0x7766CCFF), Color.RED, 2000 / 20, true, true, true);
	private boolean render;
	
	public FXWisp(World world, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		setParticleTexture(getAtlasSprite(TEXTURE_NAME[0]));
		setMaxAge(120);
		brightness = -1;//15728640;
		onUpdate();
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		Color color = iterator.next();
		setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		setAlphaF(color.getAlpha() / 255F);
	}
	
	@Override
	public boolean isTransparent() {
		return true;
	}
	
	@Override
	public void renderParticle(final VertexBuffer renderer, final Entity entity, final float tick, final float rotationX,
			final float rotationZ, final float rotationYZ, final float rotationXY, final float rotationXZ) {
        super.renderParticle(renderer, entity, tick, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
	
	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		@Override
		public Particle getEntityFX(int id, World world, double x, double y, double z, double vx, double vy, double vz, int... args) {
                return new FXWisp(world, x, y, z).addFXUpdate(FXUpdateHelper.getResultByArgs(args));
		}
	}

}