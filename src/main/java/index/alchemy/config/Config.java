package index.alchemy.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	
	public String category() default "";
	
	public String comment() default "";
	
	public float min() default Float.MIN_VALUE;
	
	public float max() default Float.MAX_VALUE;
	
}