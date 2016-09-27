package index.alchemy.development;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Test;
import index.alchemy.client.LocalTexture;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Test
@SideOnly(Side.CLIENT)
@Init(state = ModState.POSTINITIALIZED, enable = false)
public class DSkin {
	
	public static final String path = AlchemyModLoader.mc_dir + "/skin";
	
	private static final Map<ResourceLocation, Boolean> result_mapping = new HashMap<ResourceLocation, Boolean>();
	
	public static void clear() { result_mapping.clear(); }
	
	public DSkin() throws Exception {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		RenderPlayer render = createRenderFor(manager);
		FinalFieldSetter.getInstance().set(manager, RenderManager.class.getDeclaredFields()[2], render);
		Tool.<Map<String, RenderPlayer>>get(RenderManager.class, 1, manager).put("default", render);
		File dir = new File(path);
		if (!dir.isDirectory())
			dir.mkdirs();
	}
	
	public static RenderPlayer createRenderFor(RenderManager manager) {
		return new RenderPlayer(manager) {
			@Override
			protected boolean bindEntityTexture(AbstractClientPlayer entity) {
				ResourceLocation res = new ResourceLocation("skin", entity.getName());
				if (!result_mapping.containsKey(res)) {
					LocalTexture tex = new LocalTexture(new File(path, entity.getName() + ".png"));
					result_mapping.put(res, Minecraft.getMinecraft().getTextureManager().loadTexture(res, tex));
				}
				Boolean result = result_mapping.get(res);
				if (result != null && result) {
					HUDManager.bind(res);
					return true;
				}
				return super.bindEntityTexture(entity);
			}
		};
	}

}
