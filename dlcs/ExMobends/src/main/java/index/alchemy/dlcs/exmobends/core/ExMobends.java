package index.alchemy.dlcs.exmobends.core;

import baubles.client.BaublesRenderLayer;
import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.interacting.minecraft.ExItemElytra;
import net.gobbob.mobends.client.model.ModelBendsElytra;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsElytra;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static index.alchemy.dlcs.exmobends.core.ExMobends.*;
import static index.alchemy.util.Tool.$;

@Hook.Provider
@Field.Provider
@SideOnly(Side.CLIENT)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class ExMobends {
	
	public static final String
		DLC_ID = "exmobends",
		DLC_NAME = "ExMobends",
		DLC_VERSION = "0.0.1-dev";
	
	@Hook(value = "net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer#<init>", type = Hook.Type.TAIL)
	public static void init_RenderBendsPlayer(RenderBendsPlayer render, RenderManager renderManager, boolean useSmallArms) {
		init_RenderBendsPlayer(render);
	}
	
	@Hook(value = "net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer#<init>", type = Hook.Type.TAIL)
	public static void init_RenderBendsPlayer(RenderBendsPlayer render, RenderManager renderManager) {
		init_RenderBendsPlayer(render);
	}
	
	public static <V extends EntityLivingBase, U extends LayerRenderer<V>> void init_RenderBendsPlayer(RenderBendsPlayer render) {
		render.addLayer(new BaublesRenderLayer());
		try {
			U layer = $("Lvazkii.botania.client.core.handler.ContributorFancinessHandler", "new");
			if (layer != null)
				render.addLayer(layer);
			layer = $("Lvazkii.botania.client.core.handler.BaubleRenderHandler", "new");
			if (layer != null)
				render.addLayer(layer);
			layer = $("Lvazkii.botania.client.render.entity.LayerGaiaHead", "new", render.getMainModel().bipedHead);
			if (layer != null)
				render.addLayer(layer);
		} catch (Exception e) { }
	}
	
	public static final IFieldAccess<LayerBendsElytra, RenderPlayer> renderPlayer = null;
	public static final IFieldAccess<LayerBendsElytra, ModelBendsElytra> modelElytra = null;
	
	@Hook("net.gobbob.mobends.client.renderer.entity.layers.LayerBendsElytra#func_177141_a")
	public static Hook.Result doRenderLayer_elytra(LayerBendsElytra layer, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack item = ExItemElytra.getInInventoryBauble(player);
		if (item != null) {
			{
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableBlend();
				
				if (player.isPlayerInfoSet() && player.getLocationElytra() != null)
					renderPlayer.get(layer).bindTexture(player.getLocationElytra());
				else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE))
					renderPlayer.get(layer).bindTexture(player.getLocationCape());
				else
					renderPlayer.get(layer).bindTexture(LayerElytra.TEXTURE_ELYTRA);
				
				GlStateManager.pushMatrix();
				renderPlayer.get(layer).getMainModel().bipedBody.postRender(scale);
				GlStateManager.translate(0.0F, -12.0F * scale, 0.125F);
				modelElytra.get(layer).setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
				modelElytra.get(layer).render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
				
				if (item.isItemEnchanted())
					LayerArmorBase.renderEnchantedGlint(renderPlayer.get(layer), player, modelElytra.get(layer), limbSwing, limbSwingAmount,
							partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
				
				GlStateManager.popMatrix();
			}
			return Hook.Result.NULL;
		}
		return Hook.Result.VOID;
	}

}
