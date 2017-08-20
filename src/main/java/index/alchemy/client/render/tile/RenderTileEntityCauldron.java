package index.alchemy.client.render.tile;

import index.alchemy.api.annotation.Render;
import index.alchemy.client.render.HUDManager;
import index.alchemy.client.render.RenderHelper;
import index.alchemy.tile.TileEntityCauldron;
import index.alchemy.tile.TileEntityCauldron.State;
import index.alchemy.util.Always;
import index.project.version.annotation.Alpha;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Alpha
@SideOnly(Side.CLIENT)
@Render(TileEntityCauldron.class)
public class RenderTileEntityCauldron extends TileEntitySpecialRenderer<TileEntityCauldron> {
	
	public static final boolean RENDER_SIDE[] = { true, false, false, false, false, false };
	
	@Override
	public void render(TileEntityCauldron te, double tx, double ty, double tz, float partialTicks, int destroyStage, float alpha) {
		long tick = Always.getClientWorldTime();
		if (te.getState() == State.OVER && te.getContainer().size() == 1) {
			ItemStack item = te.getContainer().getFirst();
			RenderHelper.pushMatrix();
			RenderHelper.translate((float) tx + .5F, (float) ty + 1.2F, (float) tz + .5F);
			RenderHelper.rotate(tick * 4 % 360, 0, 1, 0);
			RenderHelper.scale(0.5F, 0.5F, 1F);
			Block block = Block.getBlockFromItem(item.getItem());
			if (block != null)
				RenderHelper.scale(1F, 1F, 0.5F);
			RenderHelper.renderItem(item);
			RenderHelper.popMatrix();
		} else if (te.getContainer().size() > 0) {
			final float v = 1F / 4F;
			int index = 1;
			float offsetPerPetal = 360 / te.getContainer().size();
			for (ItemStack item : te.getContainer()) {
				float offset = offsetPerPetal * index;
				float deg = (int) (tick * 2 % 360 + offset);
				RenderHelper.pushMatrix();
				RenderHelper.translate((float) tx + .5F, (float) ty + .8F, (float) tz + .25F);
				RenderHelper.translate(0, 0, v);
				RenderHelper.rotate(deg, 0, 1, 0);
				RenderHelper.translate(0, 0, -v);
				RenderHelper.scale(0.5F, 0.5F, 1F);
				Block block = Block.getBlockFromItem(item.getItem());
				if (block != null)
					RenderHelper.scale(1F, 1F, 0.5F);
				RenderHelper.renderItem(item);
				RenderHelper.popMatrix();
				index++;
			}
		}
		
		HUDManager.bind(TextureMap.LOCATION_BLOCKS_TEXTURE);
		HUDManager.restore(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		FluidStack stack = te.getTank().getFluid();
		if (stack != null) {
			float offest = (float) stack.amount / te.getTank().getCapacity();
			RenderHelper.pushMatrix();
			RenderHelper.enableBlend();
			RenderHelper.tryBlendFuncSeparate(RenderHelper.SourceFactor.SRC_ALPHA, RenderHelper.DestFactor.ONE_MINUS_SRC_ALPHA,
					RenderHelper.SourceFactor.ONE, RenderHelper.DestFactor.ZERO);
			BlockPos renderPos = te.getPos();
			RenderHelper.translateToZero();
			RenderHelper.translate(0.125F, 0.25F, 0.125F);
			RenderHelper.scaleAndCorrectThePosition(0.75F, 0.625F * offest, 0.75F,
					renderPos.getX(), renderPos.getY(), renderPos.getZ());
			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
			RenderHelper.renderBlockFluid(te.getLiquid(), renderPos, RENDER_SIDE);
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			RenderHelper.disableBlend();
			RenderHelper.popMatrix();
		}
	}

}
