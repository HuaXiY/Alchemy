package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.common.LoaderState.ModState;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Init {
    
    ModState state() default ModState.UNLOADED;
    
    int index() default 0;
    
    boolean enable() default true;
    
}