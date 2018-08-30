package index.alchemy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import index.alchemy.api.annotation.SuppressFBWarnings;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.project.version.annotation.Omega;

import static index.alchemy.util.$.$;

@Omega
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public abstract class EnumHelper extends net.minecraftforge.common.util.EnumHelper {
	
	@Nullable
	public static final Field findValuesField(Class<? extends Enum<?>> clazz) {
		Object[] values = $(clazz, ">values");
		for (Field field : $.getAllFields(clazz))
			if (Modifier.isStatic(field.getModifiers()) && field.getType().isArray() &&
					Tool.deepArrayEquals(ReflectionHelper.get(ReflectionHelper.setAccessible(field)), values))
				return field;
		return null;
	}
	
	@SafeVarargs
	@SuppressFBWarnings("NP_NULL_PARAM_DEREF")
	public static final <T extends Enum<?>> void setValues(Class<T> clazz, @Nonnull T... args) {
		Field field = findValuesField(clazz);
		if (field == null)
			AlchemyRuntimeException.onException(new NullPointerException("values"));
		try {
			FinalFieldHelper.setStatic(field, args);
		} catch (Exception e) { AlchemyRuntimeException.onException(e); }
	}

}
