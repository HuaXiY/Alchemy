package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import index.alchemy.core.AlchemyEventSystem;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EventHandler { }
    
    String[] value() default AlchemyEventSystem.EventType.Name.EVENT_BUS;
    
}
