package index.alchemy.client.fx;

import java.awt.Color;
import java.util.Iterator;

import index.alchemy.annotation.Texture;
import index.alchemy.client.color.ColorHelper;
import net.minecraft.world.World;

@Texture({
	"alchemy:particle/wisp"
})
public class FXWisp extends AlchemyFX {
	
	private static final String TEXTURE_NAME[] = FXWisp.class.getAnnotation(Texture.class).value();
	
	private Iterator<Color> iterator = ColorHelper.ahsbStep(new Color(0x66CCFF), Color.RED, 2000 / 20, true, true, true);

	public FXWisp(World world, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		setParticleTexture(getAtlasSprite(TEXTURE_NAME[0]));
		setMaxAge(80);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		Color color = iterator.next();
		setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
	}

}