package index.alchemy.api.annotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface Variable { }