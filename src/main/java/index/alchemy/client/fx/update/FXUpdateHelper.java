package index.alchemy.client.fx.update;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import index.alchemy.api.IFXUpdate;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;

@Loading
public class FXUpdateHelper {
	
	private static final List<String> STRING_LIST = new ArrayList<String>();
	private static final List<Method> METHOD_LIST = new ArrayList<Method>();
	
	public static void init(Class<?> clazz) {
		FX.UpdateProvider provider = clazz.getAnnotation(FX.UpdateProvider.class);
		if (provider != null)
			for (Method method : clazz.getMethods()) {
				FX.UpdateMethod m = method.getAnnotation(FX.UpdateMethod.class);
				if (m != null)
					if (m.value() != null)
						if (Modifier.isStatic(method.getModifiers()))
							if (method.getParameterTypes().length == 1 
							&& method.getParameterTypes()[0].isArray() && method.getParameterTypes()[0].getComponentType() == int.class)
								if (method.getReturnType() == List.class) {
									STRING_LIST.add(m.value());
									METHOD_LIST.add(method);
								} else
									AlchemyRuntimeException.onException(new ClassCastException(
											clazz + "#" + method.getName() + "() -> return type != " + List.class.getName()));
							else
								AlchemyRuntimeException.onException(new IllegalArgumentException(
										clazz + "#" + method.getName() + "() -> args != int[]"));
						else
							AlchemyRuntimeException.onException(new IllegalAccessException(
									clazz + "#" + method.getName() + "() -> is non static"));
					else
						AlchemyRuntimeException.onException(new NullPointerException(
								clazz + "#" + method.getName() + "() -> @FX.UpdateMethod.value()"));
			}
	}
	
	public static int getIdByName(String name) {
		return STRING_LIST.indexOf(name);
	}
	
	public static int[] getIntArrayByArgs(String name, int... args) {
		int result[] = new int[args.length + 1];
		System.arraycopy(args, 0, result, 1, args.length);
		return result;
	}
	
	@Nullable
	public static List<IFXUpdate> getResultByArgs(int... args) {
		Tool.checkArrayLength(args, 1);
		try {
			return (List<IFXUpdate>) Tool.getSafe(METHOD_LIST, args[0]).invoke(null, args);
		} catch (Exception e) {
			AlchemyRuntimeException.onException(e);
		}
		return null;
	}
	
}