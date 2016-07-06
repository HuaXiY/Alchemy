package index.alchemy.client.render;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

public class MagicMatrix {
	
	/*                  
	 *         /\        ---------
	 *     __ /__\ __      | | |
	 *    \  /    \  /     | | |
	 *     \/      \/      | |---  -> ha = x / 2 
	 *     /\      /\      | | |   -> hb = h - ha = h - x / 2
	 *    /__\ __ /__\     |------ -> h = ha + hb
	 *        \  /         |
	 *         \/        -------   -> x
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
	public static final double HEXAGRAM_X = 2, HEXAGRAM_R = HEXAGRAM_X / 2, HEXAGRAM_DELTA = 0.2, 
			HEXAGRAM_LA = 3.0 / (2.0 * sqrt(3)), HEXAGRAM_LB = HEXAGRAM_LA, HEXAGRAM_HA = 1, HEXAGRAM_HB = -HEXAGRAM_HA / 2;
	
	public static void renderHexagram() {
		RenderHelper.disable(GL_TEXTURE_2D, GL_LIGHTING);
		RenderHelper.Draw2D.drawCircle(HEXAGRAM_R, HEXAGRAM_DELTA, false);
		RenderHelper.Draw2D.drawTriangle(HEXAGRAM_LA, HEXAGRAM_LB, HEXAGRAM_HA, HEXAGRAM_HB, false);
		RenderHelper.Draw3D.rotateY();
		RenderHelper.Draw2D.drawTriangle(HEXAGRAM_LA, HEXAGRAM_LB, HEXAGRAM_HA, HEXAGRAM_HB, false);
		RenderHelper.Draw3D.rotateY();
		RenderHelper.enable(GL_TEXTURE_2D, GL_LIGHTING);
	}

}
