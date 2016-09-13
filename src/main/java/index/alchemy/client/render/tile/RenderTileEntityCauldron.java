package index.alchemy.client.render.tile;

import index.alchemy.api.Always;
import index.alchemy.api.annotation.Render;
import index.alchemy.client.render.MagicMatrix;
import index.alchemy.client.render.RenderHelper;
import index.alchemy.tile.TileEntityCauldron;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

@SideOnly(Side.CLIENT)
@Render(TileEntityCauldron.class)
public class RenderTileEntityCauldron extends TileEntitySpecialRenderer<TileEntityCauldron> {
	
	@Override
	public void renderTileEntityAt(TileEntityCauldron te, double tx, double ty, double tz, float partialTicks, int destroyStage) {
		final float v = 1F / 4F;
		int index = 1, size = te.getContainer().size();
		
		if (size < 1)
			return;
		
		long tick = Always.getClientWorldTime();
		float offsetPerPetal = 360 / size;
		
		glDisable(GL_LIGHTING);
		
		/*glRotatef(tick % 72 * 5, 0, 1, 0);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_LIGHTING);
		RenderHelper.setColor(Color.PINK);
		RenderHelper.Draw3D.drawRound(1, 0, 360, 0.5, false);
		RenderHelper.Draw3D.drawTriangle(1, false);
		glRotatef(180, 0, 1, 0);
		RenderHelper.Draw3D.drawTriangle(1, false);
		glRotatef(-180, 0, 1, 0);
		//RenderHelper.Draw3D.drawCube(0.1, 0.2, 0.1);
		glEnable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		glRotatef(-tick % 72 * 5, 0, 1, 0);*/
		/*float points[][] = {
			    { -4F, -4F, 0F }, { -2F, 4F, 0F },
			    { 2F, -4F, 0F }, { 4F, 4F, 0F }
		};
		FloatBuffer buffer = BufferUtils.createFloatBuffer(12);
		buffer.clear();
		for (float potion[] : points)
			buffer.put(potion);
		buffer.rewind();
		
		glMap1f(GL_MAP1_VERTEX_3, 0, 1, 3, 4, buffer);
		glEnable(GL_MAP1_VERTEX_3);
		glBegin(GL_LINE_STRIP);
	    for (int i = 0; i < 31; i++)
	        glEvalCoord1f(i / 30F);
	    glEnd();
	    glDisable(GL_MAP1_VERTEX_3);*/
		
		RenderHelper.setColor(new Color(0x66, 0xCC, 0xFF, 0x77));
		MagicMatrix.renderHexagram();
		
		// Render Cube
		/*glDisable(GL_TEXTURE_2D);
		glDisable(GL_LIGHTING);
		glEnable(GL_BLEND);
		for (int i = 0, max = 6; i < max; i++) {
			float r = 1F / 1.5F, deg = (int) (tick / 0.5F % 360F + (360 / max) * i),
					offest = (1 / 8F - 2 * abs(1 / 8F - tick * 0.2F % 10 / 40F)) * (i % 2 == 0 ? 1 : -1);
			glPushMatrix();
			glTranslatef((float) tx + .5F, (float) ty + 1.2F + offest, (float) tz + .5F - r);
			glTranslatef(0, 0, r);
			glRotatef(deg, 0, 1, 0);
			glTranslatef(0, 0, -r);
			
			RenderHelper.Draw3D.drawCube(0.05F, 0.1F, 0.05F);
			
			glPopMatrix();
		}
		glDisable(GL_BLEND);
		glEnable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);*/
		// End
		
		for(ItemStack item : te.getContainer()) {
			float offset = offsetPerPetal * index;
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
			index++;
		}
		onWorkingTick(te, tick);
	}
	
	public void onWorkingTick(TileEntityCauldron te, long tick) {
		World world = te.getWorld();
		// TODO
	}

}
