package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Handle {
		
		enum Type {
			MAKE,
			SAVE
		}
		
		String name();
		
		Type type();
		
	}
	
	String handle() default "";
	
	String category();
	
	String comment() default "";
	
	float min() default Float.MIN_VALUE;
	
	float max() default Float.MAX_VALUE;
	
}
