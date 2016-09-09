package index.alchemy.client.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import index.alchemy.api.Always;
import index.alchemy.api.annotation.Change;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.LoaderState.ModState;

//TODO
//!!!!> Only in the version 1.10 working <!!!!
//This class is used to register the EnumParticleTypes in the Minecraft.
//Not guaranteed to work in another version, Field name and
//position will change with the version.
@Loading
@Change("1.10")
@Init(state = ModState.POSTINITIALIZED)
public class AlchemyFXType {
	
    private static final Map<Integer, EnumParticleTypes> PARTICLES = Tool.get(EnumParticleTypes.class, 51);
    private static final Map<String, EnumParticleTypes> BY_NAME = Tool.get(EnumParticleTypes.class, 52);
    
    private static final Map<FX, Class<?>> FX_MAPPING = new HashMap<FX, Class<?>>();
	
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
		PARTICLES.put(type.getParticleID(), type);
        BY_NAME.put(type.getParticleName(), type);
		return null;
	}
	
	public static void init(Class<?> clazz) {
		FX fx = clazz.getAnnotation(FX.class);
		if (fx != null)
			if (fx.name() != null)
					FX_MAPPING.put(fx, clazz);
				else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @FX.name()"));
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (Entry<FX, Class<?>> entry : FX_MAPPING.entrySet()) {
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