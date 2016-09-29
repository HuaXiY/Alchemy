package index.alchemy.client.fx;

import static index.alchemy.client.color.ColorHelper.ahsbStep;

import java.awt.Color;
import java.util.Iterator;

import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.util.Tool;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FXWisp extends AlchemyFX {
	
	@Texture({
		"alchemy:particle/wisp"
	})
	@FX(name = "wisp", ignoreRange = false)
	public static class Info {
		
		public static final EnumParticleTypes type = null;
		
	}
	
	private static final String TEXTURE_NAME[] = FXWisp.Info.class.getAnnotation(Texture.class).value();
	
	private Iterator<Color> iterator = ahsbStep(new Color(0x7766CCFF), Color.RED, 2000 / 20, true, true, true);
	private boolean render;
	
	public FXWisp(World world, double posX, double posY, double posZ, int max_age) {
		super(world, posX, posY, posZ);
		setParticleTexture(getAtlasSprite(TEXTURE_NAME[0]));
		setMaxAge(max_age);
		brightness = -1;//15728640;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
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
		public Particle createParticle(int id, World world, double x, double y, double z, double vx, double vy, double vz, int... args) {
                return new FXWisp(world, x, y, z, Tool.getSafe(args, 1, 0) * 10).addFXUpdate(FXUpdateHelper.getResultByArgs(args));
		}
		
	}

}