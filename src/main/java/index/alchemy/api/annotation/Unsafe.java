package index.alchemy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Unsafe {
	
	public static final String UNSAFE_API = "sun.misc.Unsafe", ASM_API = "org.objectweb.asm", REFLECT_API = "java.lang.reflect";
	
	public String value() default "unknown";

}