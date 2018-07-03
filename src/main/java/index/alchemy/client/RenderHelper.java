package index.alchemy.client;

import index.alchemy.api.annotation.Patch;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;
import static net.minecraft.util.math.MathHelper.*;

@Omega
@SideOnly(Side.CLIENT)
public class RenderHelper extends GlStateManager {
	
	@Patch("org.lwjgl.opengl.GL11")
	public static class Patch$GL11 {
		
		public static int glGetError() {
			return GL_NO_ERROR;
		}
		
	}
	
	public static void renderItem(ItemStack item) {
		Minecraft.getMinecraft().getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.GROUND);
	}
	
	public static void renderBlock(IBlockState state, BlockPos pos) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);
		Minecraft minecraft = Minecraft.getMinecraft();
		BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
		dispatcher.renderBlock(state, pos, minecraft.world, bufferBuilder);
		tessellator.draw();
	}
	
	public static float getRenderPartialTicks() {
		return Minecraft.getMinecraft().getRenderPartialTicks();
	}
	
	public static void scaleAndCorrectThePosition(float x, float y, float z, float dx, float dy, float dz) {
		glTranslatef(dx * (1F - x), dy * (1F - y), dz * (1F - z));
		glScalef(x, y, z);
	}
	
	public static float calculateRenderOffset(float now, float last, float partialTicks) {
		return last + (now - last) * partialTicks;
	}
	
	public static void translateToZero() {
		Entity view = Tool.isNullOr(Minecraft.getMinecraft().getRenderViewEntity(), () -> Minecraft.getMinecraft().player);
		float partialTicks = getRenderPartialTicks();
		glTranslatef(
				-calculateRenderOffset((float) view.posX, (float) view.lastTickPosX, partialTicks),
				-calculateRenderOffset((float) view.posY, (float) view.lastTickPosY, partialTicks),
				-calculateRenderOffset((float) view.posZ, (float) view.lastTickPosZ, partialTicks)
		);
	}
	
	public static void enable(int... args) {
		for (int arg : args)
			glEnable(arg);
	}
	
	public static void disable(int... args) {
		for (int arg : args)
			glDisable(arg);
	}
	
	public static class Lighting extends net.minecraft.client.renderer.RenderHelper { }
	
	public static class OpenGL extends OpenGlHelper { }
	
	public static class Draw2D {
		
		public static void drawTriangle(float la, float lb, float ha, float hb, boolean fill) {
			glBegin(fill ? GL_TRIANGLE_FAN : GL_LINE_STRIP);
			glVertex3f(-la, 0, hb);
			glVertex3f(lb, 0, hb);
			glVertex3f(0, 0, ha);
			glVertex3f(-la, 0, hb);
			glEnd();
		}
		
		public static void drawCircle(float r, float delta_angle, boolean fill) {
			drawRound(r, 0, 360, delta_angle, fill);
		}
		
		public static void drawRound(float r, float start_angle, float end_angle, float delta_angle, boolean fill) {
			glBegin(fill ? GL_TRIANGLE_FAN : GL_LINE_STRIP);
			for (float i = start_angle; i < end_angle; i += delta_angle)
				glVertex3f(r * cos(i), 0 ,r * sin(i));
			glEnd();
		}
		
		public static void drawRectangle(float x, float z) {
			glBegin(GL_QUADS);
			glVertex3f(x, 0, z);
			glVertex3f(-x, 0, z);
			glVertex3f(-x, 0, -z);
			glVertex3f(x, 0, -z);
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
		
		public static void rotateX(float rot) {
			glRotatef(rot, 1, 0, 0);
		}
		
		public static void rotateY(float rot) {
			glRotatef(rot, 0, 1, 0);
		}
		
		public static void rotateZ(float rot) {
			glRotatef(rot, 0, 0, 1);
		}
		
		public static void translate(Entity entity) {
			glTranslatef((float) entity.posX, (float) entity.posY, (float) entity.posZ);
		}
		
		public static void translate(BlockPos pos) {
			glTranslatef(pos.getX(), pos.getY(), pos.getZ());
		}
		
		public static void translateCenter(BlockPos pos) {
			glTranslatef(pos.getX() + .5F, pos.getY() + .5F, pos.getZ() + .5F);
		}
		
		public static void drawRectangle(float x, float y, float z, EnumFacing facing) {
			glBegin(GL_QUADS);
			switch (facing) {
				case UP:
					glVertex3f(x, y, z);
					glVertex3f(-x, y, z);
					glVertex3f(-x, y, -z);
					glVertex3f(x, y, -z);
					break;
				case DOWN:
					glVertex3f(x, -y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(-x, -y, -z);
					glVertex3f(x, -y, -z);
					break;
				case NORTH:
					glVertex3f(x, y, z);
					glVertex3f(-x, y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(x, -y, z);
					break;
				case SOUTH:
					glVertex3f(x, y, z);
					glVertex3f(-x, y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(x, -y, z);
					break;
				case WEST:
					glVertex3f(x, y, z);
					glVertex3f(x, -y, z);
					glVertex3f(x, -y, -z);
					glVertex3f(x, y, -z);
					break;
				case EAST:
					glVertex3f(-x, y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(-x, -y, -z);
					glVertex3f(-x, y, -z);
					break;
			}
			glEnd();
			glBegin(GL_QUADS);
			switch (facing) {
				case UP:
					glVertex3f(x, y, -z);
					glVertex3f(-x, y, -z);
					glVertex3f(-x, y, z);
					glVertex3f(x, y, z);
					break;
				case DOWN:
					glVertex3f(x, -y, -z);
					glVertex3f(-x, -y, -z);
					glVertex3f(-x, -y, z);
					glVertex3f(x, -y, z);
					break;
				case NORTH:
					glVertex3f(x, -y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(-x, y, z);
					glVertex3f(x, y, z);
					break;
				case SOUTH:
					glVertex3f(x, -y, z);
					glVertex3f(-x, -y, z);
					glVertex3f(-x, y, z);
					glVertex3f(x, y, z);
					break;
				case WEST:
					glVertex3f(x, y, -z);
					glVertex3f(x, -y, -z);
					glVertex3f(x, -y, z);
					glVertex3f(x, y, z);
					break;
				case EAST:
					glVertex3f(-x, y, -z);
					glVertex3f(-x, -y, -z);
					glVertex3f(-x, -y, z);
					glVertex3f(-x, y, z);
					break;
			}
			glEnd();
		}
		
		public static void drawCube(float x, float y, float z) {
			for (EnumFacing facing : EnumFacing.VALUES)
				drawRectangle(x, y, z, facing);
		}
		
	}

}