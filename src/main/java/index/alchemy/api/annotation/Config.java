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
	public static @interface Handle {
		
		public static enum Type {
			MAKE,
			SAVE
		}
		
		public String name();
		
		public Type type();
		
	}
	
	public String handle() default "";
	
	public String category();
	
	public String comment() default "";
	
	public float min() default Float.MIN_VALUE;
	
	public float max() default Float.MAX_VALUE;
	
}