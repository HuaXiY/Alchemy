package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Patch {
	
	@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Exception { }
	
	@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Spare { }
	
	@Target({ ElementType.TYPE_USE })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Generic {
		
		String value();
		
	}
	
	String value();

}