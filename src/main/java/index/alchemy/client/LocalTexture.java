package index.alchemy.client;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IResourceManager;

public class LocalTexture implements ITextureObject {
	
	public final File file;
	private int id;
	
	public LocalTexture(File file) {
		this.file = file;
	}

	@Override
	public void setBlurMipmap(boolean blurIn, boolean mipmapIn) { }

	@Override
	public void restoreLastBlurMipmap() { }

	@Override
	public void loadTexture(IResourceManager resourceManager) throws IOException {
		id = TextureLoader.loadTexture(file);
	}

	@Override
	public int getGlTextureId() {
		return id;
	}

}