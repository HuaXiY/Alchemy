package index.alchemy.client.render;

import index.alchemy.client.color.ColorHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;
import static java.lang.Math.*;

public class RenderHelper {
	
	public static void renderItem(ItemStack item) {
		Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.GROUND);
	}
	
	public static void renderBlock(IBlockState block, BlockPos pos) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
		Minecraft minecraft = Minecraft.getMinecraft();
		BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
		dispatcher.getBlockModelRenderer().renderModel(minecraft.theWorld, dispatcher.getModelForState(block),
				block, BlockPos.ORIGIN, vertexbuffer, false, 0);
		tessellator.draw();
	}
	
	public static void renderEntity(Entity entity, double x, double y, double z, float yaw, float partialTick, boolean box) {
		Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, x, y, z, yaw, partialTick, box);
	}
	
	public static void renderEntity(Entity entity, float partialTick) {
		renderEntity(entity, 0, 0, 0, 0, partialTick, false);
	}
	
	public static void setColor(Color color) {
		ColorHelper.setColor(color);
	}
	
	public static class Draw2D {
		
		public static void drawTriangle(double l, double h, boolean fill) {
			glBegin(fill ? GL_TRIANGLE_FAN : GL_LINE_STRIP);
			glVertex3d(-l, 0, 0);
			glVertex3d(l, 0, 0);
			glVertex3d(0, 0, h);
			glVertex3d(-l, 0, 0);
			glEnd();
		}
		
		public static void drawRound(double r, double start_angle, double end_angle, double delta_angle, boolean fill) {
			glBegin(fill ? GL_TRIANGLE_FAN : GL_LINE_STRIP);
			for (double i = start_angle; i < end_angle; i += delta_angle)
				glVertex3d(r * cos(i), 0 ,r * sin(i));
			glEnd();
		}
		
		public static void drawRectangle(double x, double z) {
			glBegin(GL_QUADS);
			glVertex3d(x, 0, z);
			glVertex3d(-x, 0, z);
			glVertex3d(-x, 0, -z);
			glVertex3d(x, 0, -z);
			glEnd();
		}
		
	}

	public static class Draw3D {
		
		public static void rotateX() {
			glRotatef(180, 1, 0, 0);
		}
		
		public static void rotateY() {
			glRotatef(180, 0, 1, 0);
		}
		
		public static void rotateZ() {
			glRotatef(180, 0, 0, 1);
		}
		
		public static void translate(Entity entity) {
			glTranslated(entity.posX, entity.posY, entity.posZ);
		}
		
		public static void translate(BlockPos pos) {
			glTranslatef(pos.getX(), pos.getY(), pos.getZ());
		}
		
		public static void translateCenter(BlockPos pos) {
			glTranslatef(pos.getX() + .5F, pos.getY() + .5F, pos.getZ() + .5F);
		}
		
		public static void drawRectangle(double x, double y, double z, EnumFacing facing) {
			glBegin(GL_QUADS);
			switch (facing) {
				case UP:
					glVertex3d(x, y, z);
					glVertex3d(-x, y, z);
					glVertex3d(-x, y, -z);
					glVertex3d(x, y, -z);
					break;
				case DOWN:
					glVertex3d(x, -y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(-x, -y, -z);
					glVertex3d(x, -y, -z);
					break;
				case NORTH:
					glVertex3d(x, y, z);
					glVertex3d(-x, y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(x, -y, z);
					break;
				case SOUTH:
					glVertex3d(x, y, z);
					glVertex3d(-x, y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(x, -y, z);
					break;
				case WEST:
					glVertex3d(x, y, z);
					glVertex3d(x, -y, z);
					glVertex3d(x, -y, -z);
					glVertex3d(x, y, -z);
					break;
				case EAST:
					glVertex3d(-x, y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(-x, -y, -z);
					glVertex3d(-x, y, -z);
					break;
			}
			glEnd();
			glBegin(GL_QUADS);
			switch (facing) {
				case UP:
					glVertex3d(x, y, -z);
					glVertex3d(-x, y, -z);
					glVertex3d(-x, y, z);
					glVertex3d(x, y, z);
					break;
				case DOWN:
					glVertex3d(x, -y, -z);
					glVertex3d(-x, -y, -z);
					glVertex3d(-x, -y, z);
					glVertex3d(x, -y, z);
					break;
				case NORTH:
					glVertex3d(x, -y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(-x, y, z);
					glVertex3d(x, y, z);
					break;
				case SOUTH:
					glVertex3d(x, -y, z);
					glVertex3d(-x, -y, z);
					glVertex3d(-x, y, z);
					glVertex3d(x, y, z);
					break;
				case WEST:
					glVertex3d(x, y, -z);
					glVertex3d(x, -y, -z);
					glVertex3d(x, -y, z);
					glVertex3d(x, y, z);
					break;
				case EAST:
					glVertex3d(-x, y, -z);
					glVertex3d(-x, -y, -z);
					glVertex3d(-x, -y, z);
					glVertex3d(-x, y, z);
					break;
			}
			glEnd();
		}
		
		public static void drawCube(double x, double y, double z) {
			for (EnumFacing facing : EnumFacing.VALUES)
				drawRectangle(x, y, z, facing);
		}
		
	}

}