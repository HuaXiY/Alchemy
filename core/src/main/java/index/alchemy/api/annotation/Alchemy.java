package index.alchemy.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.meta.TypeQualifier;

import index.alchemy.core.AlchemyModLoader;

@Retention(RetentionPolicy.RUNTIME)
@TypeQualifier(applicableTo = AlchemyModLoader.class)
public @interface Alchemy {

}
