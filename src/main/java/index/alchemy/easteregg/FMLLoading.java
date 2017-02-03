package index.alchemy.easteregg;

import java.io.File;
import java.util.List;

import org.lwjgl.opengl.Display;

import com.google.common.collect.Lists;

import index.alchemy.api.annotation.Hook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyResourceLocation;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

@Hook.Provider
@SideOnly(Side.CLIENT)
public class FMLLoading {
	
	private static final IResourcePack alchemyPack;
	private static SplashProgress.Texture gifs[];
	private static int index;
	
	static {
		File alchemy = AlchemyModLoader.mod_path;
		alchemyPack = alchemy.isDirectory() ? new FolderResourcePack(alchemy) : new FileResourcePack(alchemy);
	}
	
	@Hook("net.minecraftforge.fml.client.SplashProgress$Texture#bind")
	public static void bind(SplashProgress.Texture texture) {
		if (texture != SplashProgress.forgeTexture)
			return;
		if (gifs == null) {
			List<SplashProgress.Texture> textures = Lists.newLinkedList();
			for (int i = 1; true; i++) {
				ResourceLocation loading = new AlchemyResourceLocation("textures/gui/loading/" + i + ".png");
				if (!alchemyPack.resourceExists(loading))
					break;
				textures.add(new SplashProgress.Texture(loading));
			}
			gifs = textures.toArray(new SplashProgress.Texture[textures.size()]);
		}
		int f = (++index / 10) % gifs.length;
		SplashProgress.Texture gif = gifs[f];
		gif.bind();
		float fw = (float) gif.getWidth() / 2;
		float fh = (float) gif.getHeight() / 2;
		glPushMatrix();
		glLoadIdentity();
		glTranslatef(-fw, Display.getHeight() - gif.getHeight() + fh - 10, 0);
		glBegin(GL_QUADS);
		gif.texCoord(0, 0, 0);
		glVertex2f(-fw, -fh);
		gif.texCoord(0, 0, 1);
		glVertex2f(-fw, fh);
		gif.texCoord(0, 1, 1);
		glVertex2f(fw, fh);
		gif.texCoord(0, 1, 0);
		glVertex2f(fw, -fh);
		glEnd();
		glPopMatrix();
	}
	
	@Hook(value = "net.minecraftforge.fml.client.SplashProgress#open", isStatic = true)
	public static Hook.Result open(ResourceLocation loc, boolean allowRP) {
		try {
			if (allowRP && alchemyPack.resourceExists(loc))
				return new Hook.Result(alchemyPack.getInputStream(loc));
		} catch (Exception e) { }
		return Hook.Result.VOID;
	}

}
