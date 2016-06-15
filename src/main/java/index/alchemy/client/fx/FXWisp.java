package index.alchemy.client.fx;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import akka.io.Tcp.Register;
import biomesoplenty.core.ClientProxy;
import index.alchemy.annotation.InitInstance;
import index.alchemy.annotation.Texture;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IRegister;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyEventSystem.EventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static index.alchemy.client.color.ColorHelper.*;
import static org.lwjgl.opengl.GL11.*;

@Texture({
	"alchemy:particle/wisp"
})
public class FXWisp extends AlchemyFX {
	
	private static final String TEXTURE_NAME[] = FXWisp.class.getAnnotation(Texture.class).value();
	
	private Iterator<Color> iterator = ahsbStep(new Color(0x7766CCFF), Color.RED, 2000 / 20, true, true, true);
	private boolean render;
	
	public FXWisp(World world, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
		setParticleTexture(getAtlasSprite(TEXTURE_NAME[0]));
		setMaxAge(120);
		particleScale = 0.3F;
		brightness = -1;//15728640;
		onUpdate();
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		Color color = iterator.next();
		setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		setAlphaF(color.getAlpha() / 255F);
		//System.out.println(color.getAlpha());
		particleScale -= 0.002F;
	}
	
	@Override
	public boolean isTransparent() {
		return true;
	}
	
	@Override
	public void renderParticle(final VertexBuffer renderer, final Entity entity, final float tick, final float rotationX,
			final float rotationZ, final float rotationYZ, final float rotationXY, final float rotationXZ) {
		//System.out.println(2);
		//glDisable(GL_ALPHA_TEST);
		//FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("alchemy:textures/particle/wisp.png"));
		
		//GlStateManager.depthMask(false);
        //GlStateManager.enableBlend();
        //GlStateManager.blendFunc(770, 1);

        super.renderParticle(renderer, entity, tick, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
//
        //GlStateManager.disableBlend();
        //GlStateManager.depthMask(true);
		
		//glDepthMask(false);
		
		//glDisable(GL_DEPTH_TEST);
        //glEnable(GL_ALPHA_TEST);
		
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//glAlphaFunc(GL_GREATER, 0.004F);
		
		//super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);		
		/*if (render = true)
			super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		else
			RenderFXWisp.RENDER_QUEUE.add(this);*/
		
		//glEnable(GL_DEPTH_TEST);
		//glDepthMask(true);
	}

}