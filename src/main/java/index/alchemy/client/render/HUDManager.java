package index.alchemy.client.render;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.config.Config;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HUDManager {
	
	public static final int CD_SIZE = 48, ON_ROW = 256 / CD_SIZE;
	public static final String CATEGORY = "HUD";
	public static final ResourceLocation CD_BG = new AlchemyResourceLocation("textures/gui/cd.png");
	
	@Config(category = CATEGORY, comment = "set true to reversal CD render")
	private static boolean cd_reversal = false;
	
	private static final List<ICoolDown> CD = new LinkedList<ICoolDown>();
	
	public static void registerCoolDown(ICoolDown cd) {
		if (AlchemyModLoader.getState().ordinal() >= ModState.AVAILABLE.ordinal())
			throw new AlchemyRuntimeExcption(new RuntimeException("Abnormal state: " + AlchemyModLoader.getState().name()));
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
		
		int i = 0, len = CD.size();
		for (ICoolDown cd : CD) {
			float cd_per = (float) cd.getResidualCD() / cd.getMaxCD();
			if (cd_per <= 0)
				continue;
			++i;
			int x = getCDXStart(i), y = getCDYStart(i);
			bind(CD_BG);
			gui.drawTexturedModalRect(x, y, 0, 0, CD_SIZE, CD_SIZE);
			
			int id = cd.getRenderID();
			if (id > 1)
				gui.drawTexturedModalRect(x, y, CD_SIZE * (id % ON_ROW), CD_SIZE * (id / ON_ROW), CD_SIZE, CD_SIZE);
			else
				cd.renderCD(x, y, CD_SIZE, CD_SIZE);
			
			int cd_per_len = (int) (CD_SIZE * cd_per);
			gui.drawRect(x, y + (CD_SIZE - cd_per_len), x + CD_SIZE, y + CD_SIZE, 0x77000000);
		}
			
	}
	
	public static int getCDXStart(int i) {
		// TODO
		return Minecraft.getMinecraft().displayWidth - CD_SIZE;
	}
	
	public static int getCDYStart(int i) {
		// TODO
		return Minecraft.getMinecraft().displayHeight - CD_SIZE;
	}
	
}
