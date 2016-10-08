package index.alchemy.client.render.tile;

import index.alchemy.api.annotation.Render;
import index.alchemy.client.render.HUDManager;
import index.alchemy.client.render.RenderHelper;
import index.alchemy.tile.TileEntityCauldron;
import index.alchemy.util.Always;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.client.renderer.GlStateManager.*;

@SideOnly(Side.CLIENT)
@Render(TileEntityCauldron.class)
public class RenderTileEntityCauldron extends TileEntitySpecialRenderer<TileEntityCauldron> {
	
	@Override
	public void renderTileEntityAt(TileEntityCauldron te, double tx, double ty, double tz, float partialTicks, int destroyStage) {
		if (te.getContainer().size() > 0) {
			final float v = 1F / 4F;
			int index = 1;
			long tick = Always.getClientWorldTime();
			float offsetPerPetal = 360 / te.getContainer().size();
			for (ItemStack item : te.getContainer()) {
				float offset = offsetPerPetal * index;
				float deg = (int) (tick / 0.5F % 360F + offset);
				pushMatrix();
				translate((float) tx + .5F, (float) ty + .8F, (float) tz + .25F);
				translate(0, 0, v);
				rotate(deg, 0, 1, 0);
				translate(0, 0, -v);
				scale(0.5F, 0.5F, 1F);
				Block block = Block.getBlockFromItem(item.getItem());
				if (block != null)
					scale(1F, 1F, 0.5F);
				RenderHelper.renderItem(item);
				popMatrix();
				index++;
			}
		}
		
		HUDManager.bind(TextureMap.LOCATION_BLOCKS_TEXTURE);
		HUDManager.restore(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		FluidStack stack = te.getTank().getFluid();
		if (stack != null) {
			pushMatrix();
			enableBlend();
			tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
			BlockPos renderPos = te.getPos().up();
			float offest = (float) stack.amount / te.getTank().getCapacity();
			RenderHelper.translateToZero();
			translate(0.125F, -(0.125F + (1 - 0.125F * 2) * (1 - offest)), 0.125F);
			RenderHelper.scaleAndCorrectThePosition(0.75F, 0.75F * offest, 0.75F, renderPos.getX(), renderPos.getY(), renderPos.getZ());
			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
			RenderHelper.renderBlock(te.getLiquid(), renderPos);
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			disableBlend();
			popMatrix();
		}
	}

}
