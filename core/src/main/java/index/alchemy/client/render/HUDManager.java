package index.alchemy.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import index.alchemy.api.ICoolDown;
import index.alchemy.api.annotation.Config;
import index.alchemy.client.RenderHelper;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

@Omega
@SideOnly(Side.CLIENT)
public class HUDManager {
    
    public static final int CD_SIZE = 48, MC_BG_TEXTURE_SIZE = CD_SIZE / 2, ON_ROW = 256 / CD_SIZE, INTERVAL = 4, SNAKE = 2;
    public static final String CATEGORY = "HUD";
    public static final ResourceLocation CD_BG = new AlchemyResourceLocation("textures/gui/cd.png");
    
    private static final List<ICoolDown> cool_downs = new ArrayList<ICoolDown>();
    private static final Map<ICoolDown, Integer> snakes = new HashMap<ICoolDown, Integer>();
    
    @Config(category = CATEGORY, comment = "The x-axis offset when render the CoolDown.")
    private static int render_cool_down_offset_x = -50;
    
    @Config(category = CATEGORY, comment = "The y-axis offset when render the CoolDown.")
    private static int render_cool_down_offset_y = -50;
    
    @Config(category = CATEGORY, comment = "The number of rows when render the CoolDown.")
    public static int render_num = 4;
    
    public static void registerCoolDown(ICoolDown cd) {
        AlchemyModLoader.checkState();
        cool_downs.add(cd);
    }
    
    public static void bind(ResourceLocation res) {
        if (res != null)
            Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }
    
    public static void restore(ResourceLocation res) {
        if (res != null)
            Minecraft.getMinecraft().getTextureManager().getTexture(res).restoreLastBlurMipmap();
    }
    
    public static void setupOverlayRendering() {
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
    }
    
    public static void setSnake(ICoolDown cd) {
        setSnake(cd, 20);
    }
    
    public static void setSnake(ICoolDown cd, int time) {
        snakes.put(cd, time);
    }
    
    public static void render() {
        renderCD();
    }
    
    public static void renderCD() {
        GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
        RenderHelper.enableAlpha();
        int i = -1;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = resolution.getScaledWidth() * 2;
        int height = resolution.getScaledHeight() * 2;
        for (ICoolDown cd : cool_downs) {
            float cd_per = Math.min((float) cd.getResidualCD() / cd.getMaxCD(), 1);
            if (cd_per <= 0)
                continue;
            ++i;
            int x = getCDXStart(i), y = getCDYStart(i), snake = Tool.isNullOr(snakes.get(cd), 0), sx = 0, sy = 0;
            if (snake > 0) {
                snakes.put(cd, cd.getResidualCD() == 1 ? 0 : --snake);
                sx = Tool.getRandom(-SNAKE, SNAKE);
                sy = Tool.getRandom(-SNAKE, SNAKE);
            }
            x = width - x;
            y = height - y;
            RenderHelper.pushMatrix();
            RenderHelper.scale(0.5F, 0.5F, 1F);
            RenderHelper.translate(sx, sy, 0);
            RenderHelper.scale(2F, 2F, 1F);
            glEnable(GL_COLOR_ARRAY);
            bind(GuiContainer.INVENTORY_BACKGROUND);
            gui.drawTexturedModalRect(x / 2, y / 2, 141, 166, MC_BG_TEXTURE_SIZE, MC_BG_TEXTURE_SIZE);
            RenderHelper.scale(0.5F, 0.5F, 1F);
            bind(CD_BG);
            int id = cd.getRenderID();
            if (id > -1)
                gui.drawTexturedModalRect(x, y, CD_SIZE * (id % ON_ROW), CD_SIZE * (id / ON_ROW), CD_SIZE, CD_SIZE);
            cd.renderCD(x, y, CD_SIZE, CD_SIZE);
            glDisable(GL_COLOR_ARRAY);
            int cd_per_len = (int) ((CD_SIZE - INTERVAL * 2) * cd_per);
            Gui.drawRect(x + INTERVAL, y + (CD_SIZE - INTERVAL * 2 - cd_per_len) + INTERVAL, x + CD_SIZE - INTERVAL, y + CD_SIZE - INTERVAL, 0x99000000);
            RenderHelper.scale(2F, 2F, 1F);
            RenderHelper.popMatrix();
        }
        RenderHelper.disableAlpha();
    }
    
    public static int getCDXStart(int i) {
        return (CD_SIZE + INTERVAL) * (1 + (i % render_num)) + render_cool_down_offset_x;
    }
    
    public static int getCDYStart(int i) {
        return (CD_SIZE + INTERVAL) * (1 + (i / render_num)) + render_cool_down_offset_y;
    }
    
}
