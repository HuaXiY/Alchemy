package index.alchemy.client.render;

import biomesoplenty.common.entities.EntityPixie;
import biomesoplenty.common.entities.RenderPixie;
import index.alchemy.api.annotation.Patch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Patch("biomesoplenty.common.entities.RenderPixie")
public class ExRenderPixie extends RenderPixie {
	
	@Patch.Exception
	public ExRenderPixie(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(@Patch.Generic("Lnet/minecraft/entity/EntityLiving;") EntityPixie entity,
			double x, double y, double z, float entityYaw, float partialTicks) {
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(1, 1);
//		GlStateManager.disableLighting();
//
//		char c0 = 61680;
//		int i = c0 % 65536;
//		int j = c0 / 65536;
//		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)i / 1.0F, (float)j / 1.0F);
//		GlStateManager.enableLighting();
//		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  
//		
		doRender(entity, x, y, z, entityYaw, partialTicks);
//		
//		GlStateManager.disableBlend();
	}

}
