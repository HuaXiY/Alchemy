package index.alchemy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dimension {
	
	public String name() default "";
	
	public String suffix() default "";
	
	public boolean load() default false;

}
