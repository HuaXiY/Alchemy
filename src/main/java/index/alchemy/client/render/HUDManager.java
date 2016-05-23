package index.alchemy.client.render;

import static org.lwjgl.opengl.GL11.*;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.ICoolDown;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HUDManager {
	
	public static final int CD_SIZE = 48, MC_BG_TEXTURE_SIZE = CD_SIZE / 2, ON_ROW = 256 / CD_SIZE, INTERVAL = 4;
	public static final String CATEGORY = "HUD";
	public static final ResourceLocation CD_BG = new AlchemyResourceLocation("textures/gui/cd.png");
	
	public static int render_num = 4;
	
	private static final List<ICoolDown> CD = new LinkedList<ICoolDown>();
	
	public static void registerCoolDown(ICoolDown cd) {
		AlchemyModLoader.checkState();
		CD.add(cd);
	}
	
	public static void bind(ResourceLocation res) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(res);
	}
	
	public static void render() {
		renderCD();
	}
	
	public static void renderCD() {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		GlStateManager.enableAlpha();
		
		int i = -1;
		for (ICoolDown cd : CD) {
			float cd_per = (float) cd.getResidualCD() / cd.getMaxCD();
			if (cd_per <= 0)
				continue;
			++i;
			int x = getCDXStart(i), y = getCDYStart(i);

			glEnable(GL_COLOR_ARRAY);
			bind(GuiContainer.INVENTORY_BACKGROUND);
			glScalef(2F, 2F, 1F);
			gui.drawTexturedModalRect(x / 2, y / 2, 141, 166, MC_BG_TEXTURE_SIZE, MC_BG_TEXTURE_SIZE);
			glScalef(0.5F, 0.5F, 1F);
			
			bind(CD_BG);
			int id = cd.getRenderID();
			if (id > -1)
				gui.drawTexturedModalRect(x, y, CD_SIZE * (id % ON_ROW), CD_SIZE * (id / ON_ROW), CD_SIZE, CD_SIZE);
			else
				cd.renderCD(x, y, CD_SIZE, CD_SIZE);
			
			glDisable(GL_COLOR_ARRAY);
			int cd_per_len = (int) ((CD_SIZE - 8) * cd_per);
			gui.drawRect(x + 4, y + (CD_SIZE - 8 - cd_per_len) + 4, x + CD_SIZE - 4, y + CD_SIZE - 4, 0x99000000);
		}
		GlStateManager.disableAlpha();
	}
	
	public static int getCDXStart(int i) {
		return Minecraft.getMinecraft().displayWidth - (CD_SIZE + INTERVAL) *  (1 +(i % render_num));
	}
	
	public static int getCDYStart(int i) {
		return Minecraft.getMinecraft().displayHeight - (CD_SIZE + INTERVAL) * (1 + (i / (render_num)));
	}
	
}
