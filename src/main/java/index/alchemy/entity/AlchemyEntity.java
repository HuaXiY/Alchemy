package index.alchemy.entity;

import java.util.Map;

import org.objectweb.asm.Opcodes;

import com.google.common.collect.Maps;

import index.alchemy.api.annotation.EntityMapping;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Proxy;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Loading
@Proxy.Provider
@Init(state = ModState.INITIALIZED)
public class AlchemyEntity {
	
	@Proxy(opcode = Opcodes.INVOKESPECIAL, target = "net.minecraft.entity.EntityLivingBase#func_70636_d")
	public static void onLivingUpdate(EntityLivingBase living) { }
	
	protected static final Map<Class<? extends Entity>, EntityMapping> entity_mapping = Maps.newHashMap();
	
	protected static int id = 0;
	public static synchronized int nextId() { return id++; }
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		EntityMapping mapping = clazz.getAnnotation(EntityMapping.class);
		if (mapping != null)
			if (Tool.isInstance(Entity.class, clazz))
				entity_mapping.put((Class<? extends Entity>) clazz, mapping);
			else
				AlchemyRuntimeException.onException(new RuntimeException(
						"Class<" + clazz.getName() + "> forgot to extends the Class<" + Entity.class.getName() + "> ?"));
	}
	
	public static void init() {
		entity_mapping.forEach((clazz, mapping) -> EntityRegistry.registerModEntity((Class<? extends Entity>) clazz,
				mapping.value(), nextId(), AlchemyModLoader.instance(), 80, 3, true));
	}

}
