package index.alchemy.client.render;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import index.alchemy.client.RenderHelper;
import index.project.version.annotation.Alpha;

import static net.minecraft.util.math.MathHelper.*;
import static org.lwjgl.opengl.GL11.*;

@Alpha
@SideOnly(Side.CLIENT)
public class MagicMatrix {
	
	/*                  
	 *         /\        ---------
	 *     __ /__\ __      | | |
	 *    \  /    \  /     | | |
	 *     \/      \/      | |---  -> ha = x / 2
	 *     /\      /\      | | |   -> hb = h - ha = h - x / 2
	 *    /__\ __ /__\     |------ -> h = ha + hb
	 *        \  /         |
	 *         \/        --------- -> x
	 *   |------------| -> a = 2la = 2lb = la + lb
	 *   |-----||-----| -> la, lb
	 *   
	 *         _
	 *    h = √3 / 2 * a
	 *             _
	 *    x - h = √3 / 2 * (a / 3)
	 *         _            _                   _
	 *    x = √3 / 2 * a + √3 / 2 * (a / 3) = 2√3 / 3 * a
	 *                  _
	 *    ha = x / 2 = √3 / 3 * a
	 *          _                 _
	 *    hb = √3 / 2 * a - ha = √3 / 6 * a = ha / 2
	 *                             _
	 *    x = 2 => la = lb = 3 / 2√3 , ha = hb * 2 = 1
	 */
	public static final float HEXAGRAM_X = 2, HEXAGRAM_R = HEXAGRAM_X / 2, HEXAGRAM_DELTA = 0.2F, 
			HEXAGRAM_LA = 3 / (2 * sqrt(3F)), HEXAGRAM_LB = HEXAGRAM_LA, HEXAGRAM_HA = 1, HEXAGRAM_HB = -HEXAGRAM_HA / 2;
	
	public static void renderHexagram() {
		RenderHelper.disable(GL_TEXTURE_2D, GL_LIGHTING);
		RenderHelper.Draw2D.drawCircle(HEXAGRAM_R, HEXAGRAM_DELTA, false);
		RenderHelper.Draw2D.drawTriangle(HEXAGRAM_LA, HEXAGRAM_LB, HEXAGRAM_HA, HEXAGRAM_HB, false);
		RenderHelper.Draw3D.rotateY();
		RenderHelper.Draw2D.drawTriangle(HEXAGRAM_LA, HEXAGRAM_LB, HEXAGRAM_HA, HEXAGRAM_HB, false);
		RenderHelper.Draw3D.rotateY();
		RenderHelper.enable(GL_TEXTURE_2D, GL_LIGHTING);
	}
	
	/*
	 *    / \
	 *    \  \
	 *     \  \
	 *      \  \
	 *      /  /
	 *     /  /
	 *    /  /
	 *    \ /
	 *   |--|    -> r
	 *      |--| -> r 
	 */
	
	
	public static void renderMoon() {
		RenderHelper.Draw2D.drawCircle(HEXAGRAM_R, HEXAGRAM_DELTA, false);
	}
	
	
	/*
	 *   _/\_
	 *  \    /
	 *  /_/\_\
	 */
	
	public static void renderStar() {
		
	}

}
