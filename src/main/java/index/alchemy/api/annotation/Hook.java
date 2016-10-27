package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import index.alchemy.util.Tool;

@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
	
	class Result {
		
		public static final Result VOID = new Result(), NULL = new Result(null),
				TRUE = new Result(Boolean.TRUE), FALSE = new Result(Boolean.FALSE);
		
		public final Object result;
		
		public Result() {
			this.result = Tool.VOID;
		}
		
		public Result(Object result) {
			this.result = result;
		}
		
	}
	
	enum Type { HEAD, TAIL }
	
	boolean isStatic() default false;
	
	Type type() default Type.HEAD;
	
	String disable() default "";
	
	String value();
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Provider { }

}
