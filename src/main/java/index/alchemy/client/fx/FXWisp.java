package index.alchemy.client.fx;

import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.fx.update.FXUpdateHelper;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
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
	
	public FXWisp(World world, double vX, double vY, double vZ, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ, vX, vY, vZ);
		setTransparent(true);
		setParticleTexture(getAtlasSprite(TEXTURE_NAME[0]));
	}
	
	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		
		@Override
		public Particle createParticle(int id, World world, double x, double y, double z, double vX, double vY, double vZ, int... args) {
                return new FXWisp(world, x, y, z, vX, vY, vZ).addFXUpdate(FXUpdateHelper.getResultByArgs(args)).update();
		}
		
	}

}