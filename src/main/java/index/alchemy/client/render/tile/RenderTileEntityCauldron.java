package index.alchemy.client.render.tile;

import index.alchemy.api.Alway;
import index.alchemy.api.annotation.Render;
import index.alchemy.tile.TileEntityCauldron;
import index.alchemy.util.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
@Render(TileEntityCauldron.class)
public class RenderTileEntityCauldron extends TileEntitySpecialRenderer<TileEntityCauldron> {
	
	@Override
	public void renderTileEntityAt(TileEntityCauldron te, double tx, double ty, double tz, float partialTicks, int destroyStage) {
		final float v = 1F / 4F;
		int i = 1, size = 	te.getContainer().size();
		
		if (size < 1)
			return;
		
		long tick = Alway.getClientWorldTime();
		float offsetPerPetal = 360 / size;

		for(ItemStack item : te.getContainer()) {
			float offset = offsetPerPetal * i;
			float deg = (int) (tick / 0.5F % 360F + offset);
			
			glPushMatrix();
			glTranslatef((float) tx + .5F, (float) ty + .8F, (float) tz + .25F);
			glTranslatef(0, 0, v);
			glRotatef(deg, 0, 1, 0);
			glTranslatef(0, 0, -v);
			glScalef(0.5F, 0.5F, 1F);
			Block block = Block.getBlockFromItem(item.getItem());
			if (block != null)
				glScalef(1F, 1F, 0.5F);
			RenderHelper.renderItem(item);
			glPopMatrix();
			i++;
		}
		
		onWorkingTick(te, tick);
	}
	
	public void onWorkingTick(TileEntityCauldron te, long tick) {
		World world = te.getWorld();
	}

}
