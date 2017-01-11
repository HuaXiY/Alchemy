package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {

	enum Type {
		
		DEFAULT(MinecraftForge.EVENT_BUS),
		TERRAIN(MinecraftForge.TERRAIN_GEN_BUS),
		ORE(MinecraftForge.ORE_GEN_BUS);
		private final EventBus bus;
		
		public EventBus getEventBus() {
			return bus;
		}
		
		private Type(EventBus bus) {
			this.bus = bus;
		}
		
	}

	Type[] value() default Type.DEFAULT;

}
