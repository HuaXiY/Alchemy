package index.alchemy.client;

import java.io.IOException;

import index.project.version.annotation.Omega;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;

@Omega
public class MemoryTexture extends AbstractTexture {
    
    public final byte data[];
    
    public MemoryTexture(byte data[]) { this.data = data; }
    
    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        IOException exception = null;
        for (int i = 0; glTextureId == -1 && i < 3; i++)
            try {
                glTextureId = TextureLoader.loadTexture(data);
            } catch (IOException e) {
                glTextureId = -1;
                exception = e;
            }
        if (glTextureId == -1 && exception != null)
            exception.printStackTrace();
    }
    
}
