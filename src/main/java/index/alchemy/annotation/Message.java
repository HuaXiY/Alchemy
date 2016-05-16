package index.alchemy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.relauncher.Side;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
	
	public Side value();

}