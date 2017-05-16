package index.alchemy.client;

import java.io.File;
import java.io.IOException;

import index.project.version.annotation.Omega;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;

@Omega
public class LocalTexture extends AbstractTexture {
	
	public final File file;
	
	public LocalTexture(File file) { this.file = file; }
	
	@Override
	public void loadTexture(IResourceManager resourceManager) throws IOException {
		IOException exception = null;
		for (int i = 0; glTextureId == -1 && i < 3; i++)
			try {
				glTextureId = TextureLoader.loadTexture(file);
			} catch (IOException e) {
				glTextureId = -1;
				exception = e;
			}
		if (glTextureId == -1 && exception != null)
			exception.printStackTrace();
	}
	
}
