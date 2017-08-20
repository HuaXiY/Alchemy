package index.alchemy.client.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Change;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO
// !!!!> Only in the version 1.10 working <!!!!
// This class is used to register the EnumParticleTypes in the Minecraft.
// Not guaranteed to work in another version, Field name and
// position will change with the version.
@Omega
@Loading
@Listener
@Change("1.10")
@Init(state = ModState.POSTINITIALIZED)
public class AlchemyFXType {
	
    private static final Map<FX, Class<?>> fx_mapping = new HashMap<FX, Class<?>>();
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.BOTTOM)
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
    	if (true) return;
    	// optifine
//    	Minecraft mc = Minecraft.getMinecraft();
//    	float partialTicks = event.getPartialTicks();
//    	GlStateManager.enableFog();
//    	mc.entityRenderer.enableLightmap();
//        mc.mcProfiler.endStartSection("litParticles");
//        mc.effectRenderer.renderLitParticles(mc.thePlayer, partialTicks);
//        RenderHelper.disableStandardItemLighting();
//        mc.entityRenderer.setupFog(0, partialTicks);
//        mc.mcProfiler.endStartSection("particles");
//        mc.effectRenderer.renderParticles(mc.thePlayer, partialTicks);
//        mc.entityRenderer.disableLightmap();
//        GlStateManager.disableFog();
    }
	
	@Nullable
	public static EnumParticleTypes registerParticleTypes(String name, Class factory, boolean ignoreRange) throws Exception {
		AlchemyModLoader.checkState();
		int id = EnumParticleTypes.values().length;
		if (Always.runOnClient())
			if (Tool.isInstance(IParticleFactory.class, factory))
				Minecraft.getMinecraft().effectRenderer.registerParticle(id, (IParticleFactory) factory.newInstance());
			else 
				AlchemyRuntimeException.onException(new RuntimeException(
						"Class<" + factory.getName() + "> forgot to implement the Interface<" + IParticleFactory.class.getName() + "> ?"));
		EnumParticleTypes type = EnumHelper.addEnum(EnumParticleTypes.class, name,
				new Class[] { String.class, int.class, boolean.class }, name, id, ignoreRange);
		EnumParticleTypes.PARTICLES.put(type.getParticleID(), type);
		EnumParticleTypes.BY_NAME.put(type.getParticleName(), type);
		return type;
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		FX fx = clazz.getAnnotation(FX.class);
		if (fx != null)
			if (fx.name() != null)
					fx_mapping.put(fx, clazz);
				else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @FX.name()"));
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (Entry<FX, Class<?>> entry : fx_mapping.entrySet()) {
			FX fx = entry.getKey();
			Class<?> clazz = entry.getValue();
			AlchemyModLoader.info(clazz, fx);
			try {
				Tool.setType(clazz, registerParticleTypes(fx.name(), 
						Tool.forName(clazz.getName().replace("Info", "Factory"), false), fx.ignoreRange()));
			} catch (Exception e) {
				AlchemyRuntimeException.onException(e);
			}
		}
	}

}