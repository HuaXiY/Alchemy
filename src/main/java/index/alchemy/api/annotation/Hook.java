package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import index.alchemy.util.Tool;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
	
	public static class Result {
		
		public final Object result;
		
		public Result() {
			this.result = Tool.VOID;
		}
		
		public Result(Object result) {
			this.result = result;
		}
		
	}
	
	public boolean isStatic() default false;
	
	public String value();

}
