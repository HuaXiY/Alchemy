package index.alchemy.client;

import java.io.IOException;

import index.project.version.annotation.Omega;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IResourceManager;

@Omega
public class MemoryTexture implements ITextureObject {
	
	public final byte data[];
	private int id = -1, i;
	
	public MemoryTexture(byte data[]) { this.data = data; }
	
	@Override
	public void setBlurMipmap(boolean blurIn, boolean mipmapIn) { }
	
	@Override
	public void restoreLastBlurMipmap() { }
	
	@Override
	public void loadTexture(IResourceManager resourceManager) throws IOException {
		IOException exception = null;
		while (id == -1 && i++ < 3)
			try {
				id = TextureLoader.loadTexture(data);
			} catch (IOException e) {
				id = -1;
				exception = e;
			}
		if (id == -1 && exception != null)
			exception.printStackTrace();
	}
	
	@Override
	public int getGlTextureId() { return id; }
	
}