package index.alchemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;

import static index.alchemy.util.Tool.$;

@Omega
public abstract class EnumHelper extends net.minecraftforge.common.util.EnumHelper {
	
	@Nullable
	public static final Field findValuesField(Class<? extends Enum<?>> clazz) {
		Object[] values = $(clazz, ">values");
		for (Field field : Tool.getAllFields(clazz))
			if (Modifier.isStatic(field.getModifiers()) && field.getType().isArray() &&
					Tool.deepArrayEquals(ReflectionHelper.get(ReflectionHelper.setAccessible(field)), values))
				return field;
		return null;
	}
	
	public static final <T extends Enum<?>> void setValues(Class<T> clazz, T... args) {
		Field field = findValuesField(clazz);
		if (field == null)
			AlchemyRuntimeException.onException(new NullPointerException("values"));
		try {
			FinalFieldSetter.instance().setStatic(field, args);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}

}
