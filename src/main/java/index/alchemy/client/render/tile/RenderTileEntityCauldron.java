package index.alchemy.client.render.tile;

import index.alchemy.annotation.Render;
import index.alchemy.tile.TileEntityCauldron;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
@Render(TileEntityCauldron.class)
public class RenderTileEntityCauldron extends TileEntitySpecialRenderer<TileEntityCauldron> {
	
	@Override
	public void renderTileEntityAt(TileEntityCauldron te, double tx, double ty, double tz, float tick, int destroyStage) {
		float s = 1F / 256F * 10F;
		float v = 1F / 4F;
		float w = -v * 2.5F;
		int i = 1, size = 	1;
		
		final float modifier = 6F;
		final float rotationModifier = 0.25F;
		final float radiusBase = 1.2F;
		final float radiusMod = 0.1F;

		double ticks = getWorld().getWorldTime();
		float offsetPerPetal = 360 / Math.max(3, 1);

		glPushMatrix();
		//glTranslatef(-0.05F, -0.5F, 0F);
		/*float offset = offsetPerPetal * i;
		float deg = (int) (ticks / rotationModifier % 360F + offset);
		float rad = deg * (float) Math.PI / 180F;
		float radiusX = (float) (radiusBase + radiusMod * Math.sin(ticks / modifier));
		float radiusZ = (float) (radiusBase + radiusMod * Math.cos(ticks / modifier));
		float x =  (float) (radiusX * Math.cos(rad));
		float z = (float) (radiusZ * Math.sin(rad));
		float y = (float) Math.cos((ticks + 50 * i) / 5F) / 10F;*/

		/*for(ItemStack item : te.getContainer()) {
			glPushMatrix();
			glTranslatef(x, y, z);
			float xRotate = (float) Math.sin(ticks * rotationModifier) / 2F;
			float yRotate = (float) Math.max(0.6F, Math.sin(ticks * 0.1F) / 2F + 0.5F);
			float zRotate = (float) Math.cos(ticks * rotationModifier) / 2F;

			v /= 2F;
			glTranslatef(v, v, v);
			glRotatef(deg, xRotate, yRotate, zRotate);
			glTranslatef(-v, -v, -v);
			v *= 2F;

			Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.GROUND);

			glPopMatrix();
			i++;
		}*/
		{
			float offset = offsetPerPetal * i;
			float deg = (int) (ticks / rotationModifier % 360F + offset);
			float rad = deg * (float) Math.PI / 180F;
			float radiusX = (float) (radiusBase + radiusMod * Math.sin(ticks / modifier));
			float radiusZ = (float) (radiusBase + radiusMod * Math.cos(ticks / modifier));
			float x =  (float) (radiusX * Math.cos(rad));
			float z = (float) (radiusZ * Math.sin(rad));
			float y = (float) Math.cos((ticks + 50 * i) / 5F) / 10F;
			
			ItemStack item = new ItemStack(Items.APPLE);
			glPushMatrix();
			glTranslatef((float) tx + .4F, (float) ty + .8F, (float) tz + .4F);
			float xRotate = (float) Math.sin(ticks * rotationModifier) / 2F;
			float yRotate = (float) Math.max(0.6F, Math.sin(ticks * 0.1F) / 2F + 0.5F);
			float zRotate = (float) Math.cos(ticks * rotationModifier) / 2F;

			v /= 2F;
			glTranslatef(v, v, v);
			glRotatef(deg, xRotate, yRotate, 0);
			glTranslatef(-v, -v, -v);
			v *= 2F;
			glScalef(0.5F, 0.5F, 1F);
			Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.GROUND);

			glPopMatrix();
			i++;
		}
		
		glPopMatrix();
		
		
		
	}

}
