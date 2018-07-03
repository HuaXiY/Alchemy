package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import com.google.common.collect.Maps;

import index.alchemy.util.$;

@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
	
	final class Result {
		
		public static final Result VOID = new Result(), NULL = new Result(null), ZERO = new Result(0),
				TRUE = new Result(Boolean.TRUE), FALSE = new Result(Boolean.FALSE);
		
		public final Object result;
		
		public Map<Integer, Object> stackContext;
		
		private int index;
		
		public <T> Result operationStack(int i, T obj) {
			index = i;
			return operationStack(obj);
		}
		
		public <T> Result operationStack(T obj) {
			if (stackContext == null)
				stackContext = Maps.newHashMap();
			stackContext.put(index++, obj);
			return this;
		}
		
		public Result() { this($.voidInstance()); }
		
		public <T> Result(T result) { this.result = result; }
		
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
