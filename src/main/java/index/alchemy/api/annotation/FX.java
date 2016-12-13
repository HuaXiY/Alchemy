package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FX {
	
	String name();
	
	boolean ignoreRange() default false;
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface UpdateProvider { }
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface UpdateMethod {
		
		String value();
		
	}

}